package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import com.erp.model.bi.entity.BiTargetSkuSettingEntity;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.bi.enums.TargetSearchTypeEnum;
import com.erp.model.bi.vo.QuarterMonthSalesVO;
import com.erp.model.bi.vo.TargetAnalysisVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.mapper.DmpOrderInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 目标相关报表实现
 * @date 2023/9/14 12:23
 */
@Service
public class BiTargetReportServiceImpl implements BiTargetReportService {

    @Resource
    private BiTargetStaffSettingService biTargetStaffSettingService;

    @Resource
    private BiTargetShopSettingService biTargetShopSettingService;

    @Resource
    private BiTargetCategorySettingService biTargetCategorySettingService;

    @Resource
    private BiTargetSkuSettingService biTargetSkuSettingService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpOrderInfoMapper dmpOrderInfoMapper;

    @Override
    public LinkedHashMap<String, Object> targetFinish(TargetFinishDTO.ParamDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);

        // 获取年度开始时间和结束时间
        if (TargetSearchTypeEnum.DEPT.equals(dto.getSearchType()) ) {
            //根据指标查询部门目标值
            List<BiTargetStaffSettingEntity> list = biTargetStaffSettingService.listDeptTargetFinish(dto);
            if (CollectionUtils.isEmpty(list)) {

            }
            // 查询目标数据
            Map<Integer, BigDecimal> monthTargetMap = new HashMap<>(12);
            Map<String, List<BiTargetStaffSettingEntity>> map = list.stream().collect(Collectors.groupingBy(BiTargetStaffSettingEntity::getStaffId));
            for (Map.Entry<String, List<BiTargetStaffSettingEntity>> entry : map.entrySet()) {
                List<BiTargetStaffSettingEntity> value = entry.getValue();
                MonthEnum[] values = MonthEnum.values();
                for (MonthEnum monthEnum : values) {
                    BigDecimal decimal = value.stream().filter(obj -> monthEnum.getValue().equals(obj.getMonth())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse(BigDecimal.ZERO);
                    monthTargetMap.put(monthEnum.getValue(),decimal);
                }
            }
        }
        if (TargetSearchTypeEnum.USER.equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            List<BiTargetStaffSettingEntity> list = biTargetStaffSettingService.listUserTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SHOP.equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            List<BiTargetShopSettingEntity> list = biTargetShopSettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.CATEGORY.equals(dto.getSearchType())) {
            //根据指标查询品类目标值
            List<BiTargetCategorySettingEntity> list = biTargetCategorySettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SKU.equals(dto.getSearchType())) {
            //根据指标查询SKU目标值
            List<BiTargetSkuSettingEntity> list = biTargetSkuSettingService.listTargetFinish(dto);
        }

        // 查询销售额
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        String groupByStr = flag ? "platform_create_time" : "delivery_time";
        qw.select("SUM(COALESCE(order_fee, 0)) as order_fee", groupByStr);
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        Map<Integer, BigDecimal> monthMap = entityList.stream().collect(Collectors.groupingBy(x ->
                        // 按照月分组
                        (flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue(),
                // 对销售额进行求和
                Collectors.reducing(BigDecimal.ZERO, DmpOrderInfoEntity::getOrderFee, BigDecimal::add)
        ));
        // 计算完成率
        return null;//getMonthResultList(monthTargetMap,monthMap,start.getYear());
    }


    private List<DmpOrderInfoEntity> getOrderInfoEntities(BiFilterDTO dto, QueryWrapper<DmpOrderInfoEntity> qw, LocalDateTime start, LocalDateTime end, String groupStr) {
        boolean flag1 = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        boolean flag2 = TimeTypeEnum.DELIVERY_TIME.getCode() == dto.getTimeType();
        qw.ge(flag1, "platform_create_time", start)
                .le(flag1, "platform_create_time", end)
                .ge(flag2, "delivery_time", start)
                .le(flag2, "delivery_time", end)
                .groupBy(StringUtils.isNotBlank(groupStr), groupStr)
                .last(StringUtils.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql());
        List<DmpOrderInfoEntity> entityList = dmpOrderInfoMapper.selectList(qw);
        return entityList;
    }

    private TargetAnalysisVO<QuarterMonthSalesVO> getMonthResultList(Map<Integer, BigDecimal> quarterTargetMap, Map<Integer, BigDecimal> quarterMap, Integer year) {
        LinkedList<QuarterMonthSalesVO> resultList = new LinkedList<>();
        quarterMap.entrySet().stream().forEach(x -> {
            resultList.add(new QuarterMonthSalesVO(quarterTargetMap.get(x.getKey()), quarterMap.get(x.getKey()), null, x.getKey()));
        });
        TargetAnalysisVO<QuarterMonthSalesVO> vo = new TargetAnalysisVO<>();
        vo.setList(resultList);
        // 年度销售额
        QuarterMonthSalesVO yearSales = new QuarterMonthSalesVO(quarterTargetMap, quarterMap, year);
        HashMap<String, BigDecimal> yearMap = new LinkedHashMap<>();
        yearMap.put(yearSales.getDimension(), yearSales.getRealAmount());
        yearMap.put("目标销售额", yearSales.getTargetAmount());
        yearMap.put("完成率", yearSales.getCompletionRate());
        vo.setYearSalesTarget(yearMap);
        return vo;
    }
}
