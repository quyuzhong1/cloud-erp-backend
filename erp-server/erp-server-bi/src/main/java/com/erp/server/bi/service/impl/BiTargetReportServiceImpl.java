package com.erp.server.bi.service.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.utils.MathUtil;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.bi.enums.TargetSearchTypeEnum;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
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

    @Resource
    private DmpOrderItemService dmpOrderItemService;



    @Override
    public LinkedHashMap<String, Object> targetFinish(TargetFinishDTO.ParamDTO dto) {

        LinkedHashMap<String,Object> resultMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();

        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);

        //目标数据
        List<TargetFinishDTO.ViewDTO> targetList = listTarget(dto);
        //实际数据
        List<TargetFinishDTO.ViewDTO> realList = listReal(dto,start,end);

        //表头数据
        headMap.put("typeName",TargetSearchTypeEnum.getByCode(dto.getSearchType()));
        headMap.put("yearTotalTarget","累计年度目标");
        headMap.put("yearTotalReal","累计年度实际");
        headMap.put("rate","完成率");
        MonthEnum[] values = MonthEnum.values();
        for (MonthEnum monthEnum : values) {
            headMap.put(monthEnum.getCode(),monthEnum.getValue());
        }
        resultMap.put("head",headMap);
        //列表数据
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(targetList)) {
            Map<String, List<TargetFinishDTO.ViewDTO>> map = targetList.stream().collect(Collectors.groupingBy(TargetFinishDTO.ViewDTO::getTypeName));

            for (Map.Entry<String, List<TargetFinishDTO.ViewDTO>> entry : map.entrySet()) {
                Map<String, Object> result = new HashMap<>();
                List<TargetFinishDTO.ViewDTO> value = entry.getValue();
                //年目标
                BigDecimal yearTotalTarget = value.stream().map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                result.put("yearTotalTarget",yearTotalTarget);
                //年实际
                BigDecimal yearTotalReal = realList.stream().map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                result.put("yearTotalReal",yearTotalReal);
                BigDecimal yearRate = MathUtil.divide(yearTotalReal,yearTotalTarget);
                result.put("rate",yearRate);
                for (TargetFinishDTO.ViewDTO m : value) {
                    TargetFinishDTO.SlotDTO slotDTO = new TargetFinishDTO.SlotDTO();
                    //实际值
                    BigDecimal realValue = realList.stream().filter(obj -> obj.getMonth().equals(m.getMonth()) && obj.getTypeName().equals(m.getTypeName())).map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    slotDTO.setValue(realValue);
                    //完成率
                    BigDecimal rate = MathUtil.compareTo(m.getValue(), BigDecimal.ZERO) == MathUtil.ZERO ? BigDecimal.ZERO : MathUtil.divide(realValue, m.getValue()).multiply(MathUtil.BigDecimal_100);
                    slotDTO.setRate(rate);
                    result.put(String.valueOf(m.getMonth()), slotDTO);
                };
                resultList.add(result);
            }
        };
        resultMap.put("data",resultList);
        return resultMap;
    }


    private List<TargetFinishDTO.ViewDTO> listTarget (TargetFinishDTO.ParamDTO dto) {
        //目标数据
        List<TargetFinishDTO.ViewDTO> list = new ArrayList<>();

        // 获取年度开始时间和结束时间
        if (TargetSearchTypeEnum.DEPT.getCode().equals(dto.getSearchType()) ) {
            //根据指标查询部门目标值
            list = biTargetStaffSettingService.listDeptTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            list = biTargetStaffSettingService.listUserTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            list = biTargetShopSettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType())) {
            //根据指标查询品类目标值
            list = biTargetCategorySettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())) {
            //根据指标查询SKU目标值
            list = biTargetSkuSettingService.listTargetFinish(dto);
        }
        return list;
    }


    /**
     * @description: 查询实际数据
     * @author Will
     * @date: 2023/9/18 11:34
     * @param dto
     * @param start
     * @param end
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> listReal(TargetFinishDTO.ParamDTO dto,LocalDateTime start,LocalDateTime end) {
        //主表数据
        List<DmpOrderInfoEntity> mainList;
        //明细数据
        List<DmpOrderItemEntity> detailList = new ArrayList<>();
        if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType())
                || TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())
                || MetricsEnum.SALES_QTY.getCode().equals(dto.getMetrics())) {
            Pair<List<DmpOrderInfoEntity>, List<DmpOrderItemEntity>> listPair = listOrderItem(dto, start, end);
            mainList = listPair.getKey();
            detailList = listPair.getValue();
        } else {
            mainList = listOrderInfo(dto, start, end);
        }
        List<TargetFinishDTO.ViewDTO> resultList = getOrderAmountOrVolume(dto, mainList, detailList);
        return resultList;
    }

    /**
     * @description: 字段转换
     * @author Will
     * @date: 2023/9/18 11:34
     * @param searchType
     * @return String
     */
    private String getFieldName (String searchType) {

        String fieldName = "";
        if (TargetSearchTypeEnum.DEPT.getCode().equals(searchType) ) {
            fieldName = "dept_id";
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(searchType)) {
            fieldName = "charge_id";
        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(searchType)) {
            fieldName = "shop_name";
        }
        if (TargetSearchTypeEnum.CATEGORY.getCode().equals(searchType)) {
            fieldName = "category_id";
        }
        if (TargetSearchTypeEnum.SKU.getCode().equals(searchType)) {
            fieldName = "sku_No";
        }
        return fieldName;
    }

    /**
     * @description: 获取销售额或销量
     * @author Will
     * @date: 2023/9/18 11:34
     * @param dto
     * @param mainList
     * @param detailList
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> getOrderAmountOrVolume (TargetFinishDTO.ParamDTO dto,List<DmpOrderInfoEntity> mainList,List<DmpOrderItemEntity> detailList) {
        List<TargetFinishDTO.ViewDTO> resultList = new ArrayList<>();
        if (MetricsEnum.SALES_AMOUNT.getCode().equals(dto.getMetrics())) {
            if (CollectionUtils.isEmpty(mainList)) {
                return resultList;
            }
            resultList = groupOrderSalesAmount(mainList, detailList, dto);
        }

        if (MetricsEnum.SALES_QTY.getCode().equals(dto.getMetrics())) {

            resultList = groupOrderSalesVolume(mainList, detailList, dto);
        }
        return resultList;
    }

    /**
     * @description: 查询主表数据
     * @author Will
     * @date: 2023/9/18 11:27
     * @param dto
     * @param start
     * @param end
     * @return List<DmpOrderInfoEntity>
     */
    private List<DmpOrderInfoEntity> listOrderInfo (TargetFinishDTO.ParamDTO dto,LocalDateTime start,LocalDateTime end) {
        //根据主表数据进行分组
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        String groupByStr = flag ? "platform_create_time" : "delivery_time";
        groupByStr = groupByStr.concat(",").concat(getFieldName(dto.getSearchType()));
        qw.select("SUM(COALESCE(order_fee, 0) * currency_rate) as order_fee", groupByStr);
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        return entityList;
    }

    /**
     * @description: 查询明细数据
     * @author Will
     * @date: 2023/9/18 11:27
     * @param dto
     * @param start
     * @param end
     * @return Pair<List<List<DmpOrderItemEntity>>
     */
    private  Pair<List<DmpOrderInfoEntity>,List<DmpOrderItemEntity>> listOrderItem (TargetFinishDTO.ParamDTO dto,LocalDateTime start,LocalDateTime end) {
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        qw.select("id", "platform_create_time", "delivery_time","sell_price","currency_rate", getFieldName(dto.getSearchType()));
        List<DmpOrderInfoEntity> mainList = getOrderInfoEntities(dto, qw, start, end,null);
        if (CollectionUtils.isEmpty(mainList)) {
            return new Pair<>(Collections.EMPTY_LIST,Collections.EMPTY_LIST);
        }
        List<String> orderIds = mainList.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> detailList = dmpOrderItemService.listByOrderInfoIds(orderIds);
        return new Pair<>(mainList,detailList);
    }


    /**
     * @description: 分组获得实际数据
     * @author Will
     * @date: 2023/9/18 11:36
     * @param mainList
     * @param detailList
     * @param dto
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> groupOrderSalesAmount (List<DmpOrderInfoEntity> mainList,List<DmpOrderItemEntity> detailList,TargetFinishDTO.ParamDTO dto) {

        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();

        Map<String, BigDecimal> map = new HashMap<>();

        if (TargetSearchTypeEnum.DEPT.getCode().equals(dto.getSearchType()) ) {
            //根据指标查询部门目标值
             map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptId().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getOrderFee(), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeId().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getOrderFee(), BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
             map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getOrderFee(), BigDecimal::add))
            );
        }
        //根据指标查询品类目标值
        Map<String, Integer> monthMap = mainList.stream().collect(Collectors.toMap(obj -> obj.getId(), obj -> (flag ? obj.getPlatformCreateTime() : obj.getDeliveryTime()).getMonthValue()));
        if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType())) {
            map = detailList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getCategoryId().concat(",").concat(String.valueOf(monthMap.get(x.getOrderId()))),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.multiply(e.getSellPrice(),e.getCurrencyRate()), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())) {
            //根据指标查询SKU目标值
            map = detailList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getSkuNo().concat(",").concat(String.valueOf(monthMap.get(x.getOrderId()))),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.multiply(e.getSellPrice(),e.getCurrencyRate()), BigDecimal::add))
            );
        }
        List<TargetFinishDTO.ViewDTO> resultList = new ArrayList<>();
        map.entrySet().stream().forEach(obj -> {
            String key = obj.getKey();
            String typeName = key.split(",")[0];
            Integer month = StringUtils.isBlank(key.split(",")[1]) ? null :  Integer.valueOf(key.split(",")[1]);
            TargetFinishDTO.ViewDTO viewDTO = new TargetFinishDTO.ViewDTO();
            viewDTO.setTypeName(typeName);
            viewDTO.setMonth(month);
            viewDTO.setValue(obj.getValue());
            resultList.add(viewDTO);
        });
        return resultList;
    }

    /**
     * @description: 查询实际销量
     * @author Will
     * @date: 2023/9/18 12:00
     * @param mainList
     * @param detailList
     * @param dto
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> groupOrderSalesVolume (List<DmpOrderInfoEntity> mainList,List<DmpOrderItemEntity> detailList,TargetFinishDTO.ParamDTO dto) {

        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();

        Map<String, BigDecimal> map = new HashMap<>();

        if (TargetSearchTypeEnum.DEPT.getCode().equals(dto.getSearchType()) ) {
            //根据指标查询部门目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptId().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> detailList.stream().filter(obj -> obj.getOrderId().equals(e.getId())).map(obj -> new BigDecimal(obj.getQuantity())).reduce(BigDecimal.ZERO,BigDecimal::add), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeId().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> detailList.stream().filter(obj -> obj.getOrderId().equals(e.getId())).map(obj -> new BigDecimal(obj.getQuantity())).reduce(BigDecimal.ZERO,BigDecimal::add), BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> detailList.stream().filter(obj -> obj.getOrderId().equals(e.getId())).map(obj -> new BigDecimal(obj.getQuantity())).reduce(BigDecimal.ZERO,BigDecimal::add), BigDecimal::add))
            );
        }
        //根据指标查询品类目标值
        Map<String, Integer> monthMap = mainList.stream().collect(Collectors.toMap(obj -> obj.getId(), obj -> (flag ? obj.getPlatformCreateTime() : obj.getDeliveryTime()).getMonthValue()));
        if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType())) {
            map = detailList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getCategoryId().concat(",").concat(String.valueOf(monthMap.get(x.getOrderId()))),
                    Collectors.reducing(BigDecimal.ZERO, e -> new BigDecimal(e.getQuantity()), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())) {
            //根据指标查询SKU目标值
            map = detailList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getSkuNo().concat(",").concat(String.valueOf(monthMap.get(x.getOrderId()))),
                    Collectors.reducing(BigDecimal.ZERO, e -> new BigDecimal(e.getQuantity()), BigDecimal::add))
            );
        }
        List<TargetFinishDTO.ViewDTO> resultList = new ArrayList<>();
        map.entrySet().stream().forEach(obj -> {
            String key = obj.getKey();
            String typeName = key.split(",")[0];
            Integer month = StringUtils.isBlank(key.split(",")[1]) ? null :  Integer.valueOf(key.split(",")[1]);
            TargetFinishDTO.ViewDTO viewDTO = new TargetFinishDTO.ViewDTO();
            viewDTO.setTypeName(typeName);
            viewDTO.setMonth(month);
            viewDTO.setValue(obj.getValue());
            resultList.add(viewDTO);
        });
        return resultList;
    }

    /**
     * @description: 查询订单数据
     * @author Will
     * @date: 2023/9/18 9:50
     * @param dto
     * @param qw
     * @param start
     * @param end
     * @param groupStr
     * @return List<DmpOrderInfoEntity>
     */
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
}
