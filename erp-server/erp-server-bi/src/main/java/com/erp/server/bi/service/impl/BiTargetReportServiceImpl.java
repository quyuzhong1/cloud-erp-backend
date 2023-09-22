package com.erp.server.bi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.RedisService;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.bi.enums.MonthEnum;
import com.erp.model.bi.enums.TargetFinishViewTypeEnum;
import com.erp.model.bi.enums.TargetSearchTypeEnum;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.mapper.DmpOrderInfoMapper;
import com.erp.server.bi.mapper.DmpRefundInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private DmpRefundInfoMapper dmpRefundInfoMapper;

    @Resource
    private DmpOrderInfoMapper dmpOrderInfoMapper;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private BiDataSourceCostService biDataSourceCostService;

    @Resource
    private RedisService redisService;

    public BiTargetReportServiceImpl() {
    }

    @Override
    public LinkedHashMap<String, Object> targetFinish(TargetFinishDTO.ParamDTO dto) {

        LinkedHashMap<String,Object> resultMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();

        Integer year = LocalDate.now().getYear();
        if (StringUtils.isNotBlank(dto.getYear())) {
            year = Integer.valueOf(dto.getYear());
        }
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(year,1,1,0,0,0);
        LocalDateTime end = LocalDateTime.of(year,12,31,23,59,59, LocalTime.MAX.getNano());
        dto.setStartTime(start);
        dto.setEndTime(end);
        //目标数据
        List<TargetFinishDTO.ViewDTO> targetList = listTarget(dto);

        // TOOD 临时注释
        //实际数据
//        List<TargetFinishDTO.ViewDTO> realList = listReal(dto);
        List<TargetFinishDTO.ViewDTO> realList = Collections.emptyList();


        //表头数据
        headMap.put("typeName",TargetSearchTypeEnum.getByCode(dto.getSearchType()));
        headMap.put("totalName","累计年度目标/完成率");
        MonthEnum[] values = MonthEnum.values();
        for (MonthEnum monthEnum : values) {
            headMap.put(monthEnum.getCode(), StrUtil.format("{}年{}月",start.getYear(),monthEnum.getValue()));
        }
        resultMap.put("head",headMap);

        //部门
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            List<String> deptDataList = targetList.stream().map(TargetFinishDTO.ViewDTO::getTypeId).collect(Collectors.toList());
            List<SysDepartmentDTO> deptList = sysUserFeign.listSameLevelDeptIdList(deptDataList);
            if (CollectionUtils.isNotEmpty(deptList)) {
                if (CollectionUtils.isNotEmpty(targetList))  {
                    for (TargetFinishDTO.ViewDTO viewDTO : targetList) {
                        String typeName = deptList.stream().filter(obj -> obj.getChildrenList().stream().map(SysDepartmentDTO::getName).collect(Collectors.toList()).contains(viewDTO.getTypeName())).map(SysDepartmentDTO::getName).findFirst().orElse("");
                        viewDTO.setTypeName(typeName);
                    }
                }
                if (CollectionUtils.isNotEmpty(realList)) {
                    for (TargetFinishDTO.ViewDTO viewDTO : realList) {
                        String typeName = deptList.stream().filter(obj -> obj.getChildrenList().stream().map(SysDepartmentDTO::getName).collect(Collectors.toList()).contains(viewDTO.getTypeName())).map(SysDepartmentDTO::getName).findFirst().orElse("");
                        viewDTO.setTypeName(typeName);
                    }
                }
            }
        }

        //列表数据
        List<LinkedHashMap<String, Object>> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(targetList)) {
            Map<String, List<TargetFinishDTO.ViewDTO>> map = targetList.stream().collect(Collectors.groupingBy(TargetFinishDTO.ViewDTO::getTypeName));

            for (Map.Entry<String, List<TargetFinishDTO.ViewDTO>> entry : map.entrySet()) {
                LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                List<TargetFinishDTO.ViewDTO> value = entry.getValue();
                result.put("typeName",entry.getKey());

                TargetFinishDTO.TotalSlotDTO totalSlotDTO = new TargetFinishDTO.TotalSlotDTO();
                //年目标
                BigDecimal yearTotalTarget = value.stream().map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                totalSlotDTO.setYearTotalTarget(yearTotalTarget);
                //年实际
                BigDecimal yearTotalReal = realList.stream().filter(obj -> obj.getTypeName().equals(entry.getKey())).map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                totalSlotDTO.setYearTotalReal(yearTotalReal);
                BigDecimal yearRate = MathUtil.divide(yearTotalReal,yearTotalTarget);
                totalSlotDTO.setRate(MathUtil.multiply(yearRate,MathUtil.BigDecimal_100));
                result.put("totalName",totalSlotDTO);
                for (MonthEnum monthEnum : values) {
                    TargetFinishDTO.SlotDTO slotDTO = new TargetFinishDTO.SlotDTO();
                    //目标值
                    BigDecimal targetValue = value.stream().filter(obj -> obj.getMonth().equals(monthEnum.getValue())).map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    slotDTO.setTargetValue(targetValue);
                    //实际值
                    BigDecimal realValue = realList.stream().filter(obj -> obj.getMonth().equals(monthEnum.getValue()) && obj.getTypeName().equals(entry.getKey())).map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    slotDTO.setValue(realValue);
                    BigDecimal rate = BigDecimal.ZERO;
                    if (TargetFinishViewTypeEnum.FINISH_RATE.getCode().equals(dto.getViewType())) {
                        //完成率
                        rate = MathUtil.divide(realValue, targetValue).multiply(MathUtil.BigDecimal_100);
                    } else  {
                        //占比
                        rate = MathUtil.divide(realValue,yearTotalReal).multiply(MathUtil.BigDecimal_100);
                    }
                    slotDTO.setRate(rate);
                    result.put(monthEnum.getCode(), slotDTO);
                }
                resultList.add(result);
            }
        };
        resultMap.put("data",resultList);
        return resultMap;
    }


    @Override
    public void exportExcel(TargetFinishDTO.ParamDTO dto, HttpServletResponse response) {
        //查询所有数据
        LinkedHashMap<String, Object> resultMap = targetFinish(dto);
        LinkedList<String> headList = new LinkedList<>();
        //表头数据
        headList.add(TargetSearchTypeEnum.getByCode(dto.getSearchType()));
        headList.add("指标");
        headList.add("年累计");
        MonthEnum[] values = MonthEnum.values();
        Integer year = LocalDate.now().getYear();
        if (StringUtils.isNotBlank(dto.getYear())) {
            year = Integer.valueOf(dto.getYear());
        }
        for (MonthEnum monthEnum : values) {
            headList.add(StrUtil.format("{}年{}月",year,monthEnum.getValue()));
        }
        //表格数据
        List<LinkedHashMap<String, Object>> list = (List<LinkedHashMap<String, Object>>)resultMap.get("data");
        List<LinkedHashMap<String, Object>> exportList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(list)) {
            for (LinkedHashMap<String, Object> map : list) {
                //年度数据
                TargetFinishDTO.TotalSlotDTO totalSlotDTO = BeanUtil.toBean(map.get("totalName"), TargetFinishDTO.TotalSlotDTO.class);

                //目标数据
                LinkedHashMap<String, Object> targetMap = new LinkedHashMap<>();
                //类型名称
                targetMap.put("typeName",map.get("typeName"));
                //指标
                targetMap.put("metrics","目标值");
                //年累计
                targetMap.put("yearTotal",totalSlotDTO.getYearTotalTarget().stripTrailingZeros().toPlainString());

                //实际数据
                LinkedHashMap<String, Object> realMap = new LinkedHashMap<>();
                //类型名称
                realMap.put("typeName",map.get("typeName"));
                //指标
                realMap.put("metrics",MetricsEnum.getNameByCode(dto.getMetrics()));
                //年累计
                realMap.put("yearTotal",totalSlotDTO.getYearTotalReal().stripTrailingZeros().toPlainString());

                //完成率/占比
                LinkedHashMap<String, Object> rateMap = new LinkedHashMap<>();
                //类型名称
                rateMap.put("typeName",map.get("typeName"));
                //指标
                rateMap.put("metrics",TargetFinishViewTypeEnum.getNameByCode(dto.getViewType()));
                //年累计
                rateMap.put("yearTotal",TargetFinishViewTypeEnum.FINISH_RATE.getCode().equals(dto.getViewType()) ? (totalSlotDTO.getRate().stripTrailingZeros().toPlainString() + "%") : "100%" );

                for (MonthEnum monthEnum : values) {
                    TargetFinishDTO.SlotDTO slotDTO = BeanUtil.toBean(map.get(monthEnum.getCode()), TargetFinishDTO.SlotDTO.class);
                    targetMap.put(monthEnum.getCode(),slotDTO.getTargetValue().stripTrailingZeros().toPlainString());
                    realMap.put(monthEnum.getCode(),slotDTO.getValue().stripTrailingZeros().toPlainString());
                    rateMap.put(monthEnum.getCode(),slotDTO.getRate().stripTrailingZeros().toPlainString() + "%");
                }
                exportList.add(targetMap);
                exportList.add(realMap);
                exportList.add(rateMap);
            }
        }

        String head = "业绩目标完成";
        String fileName = redisService.getFileName("业绩目标完成导出")+ ".xlsx";
        ExcelUtil.easyUtilStr(headList,head,exportList,fileName, response);
        return;
    }


    /**
     * @description: 查询目标数据
     * @author Will
     * @date: 2023/9/19 16:34
     * @param dto
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> listTarget (TargetFinishDTO.ParamDTO dto) {
        //目标数据
        List<TargetFinishDTO.ViewDTO> list = new ArrayList<>();

        // 获取年度开始时间和结束时间
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
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
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> listReal(TargetFinishDTO.ParamDTO dto) {

        List<TargetFinishDTO.ViewDTO> resultList = new ArrayList<>();
        //需要查询订单数据的类型
        if (MetricsEnum.SALES_AMOUNT.getCode().equals(dto.getMetrics())
                || MetricsEnum.SALES_QTY.getCode().equals(dto.getMetrics())
                || MetricsEnum.NET_SALES_AMOUNT.getCode().equals(dto.getMetrics())) {
            //主表数据
            List<DmpOrderInfoEntity> mainList = dmpOrderInfoMapper.listBiFilter(dto);
            if (CollectionUtils.isEmpty(mainList)) {
                return resultList;
            }
            //明细数据 (搜索类型为品类、sku、销量需要查询明细)
            List<DmpOrderItemEntity> detailList = new ArrayList<>();
            if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType())
                    || TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())
                    || MetricsEnum.SALES_QTY.getCode().equals(dto.getMetrics())) {
                detailList = mainList.stream().flatMap(obj -> Stream.of(obj.getItemList().stream().toArray(DmpOrderItemEntity[]::new))).collect(Collectors.toList());
            }
            //销售额
            if (MetricsEnum.SALES_AMOUNT.getCode().equals(dto.getMetrics())) {
                resultList = groupOrderSalesAmount(mainList, detailList, dto);
            }
            //销量
            if (MetricsEnum.SALES_QTY.getCode().equals(dto.getMetrics())) {
                resultList = groupOrderSalesVolume(mainList, detailList, dto);
            }
            //净销售额
            if (MetricsEnum.NET_SALES_AMOUNT.getCode().equals(dto.getMetrics())) {
                //品类和SKU无需显示（现马帮数据退款明细无金额暂不计算财务销售额·6）
                if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())) {
                    return resultList;
                }
                //退款数据
                List<DmpRefundInfoEntity> refundList = listReturnOrderInfo(mainList);
                resultList = groupNetSalesAmount(mainList, refundList, dto);
            }
            return resultList;
        }
        // 财务销售额(主营收入)
        if (MetricsEnum.FINANCE_SALES_AMOUNT.getCode().equals(dto.getMetrics())) {

            resultList = groupOrderFinanceSalesAmount(dto);
        }
        //毛利润 (主营收入-成本合计)
        if (MetricsEnum.GROSS_PROFIT.getCode().equals(dto.getMetrics())) {

            resultList = groupOrderSalesProfit(dto);
        }
        //毛利率
        if (MetricsEnum.GROSS_PROFIT_RATE.getCode().equals(dto.getMetrics())) {

            resultList = groupOrderSalesRatio(dto);
        }
        return resultList;
    }

   /**
    * @description: 查询退货数据
    * @author Will
    * @date: 2023/9/19 10:00
    * @param mainList
    * @return List<DmpReturnInfoEntity>
    */
    private List<DmpRefundInfoEntity> listReturnOrderInfo (List<DmpOrderInfoEntity> mainList) {
        if (CollectionUtils.isEmpty(mainList)) {
            return Collections.EMPTY_LIST;
        }
        List<String> platformOrderIdList = mainList.stream().map(DmpOrderInfoEntity::getPlatformOrderId).collect(Collectors.toList());
        //根据订单id查询退货数据
        QueryWrapper<DmpRefundInfoEntity> qw = new QueryWrapper<>();
        qw.select("platform_order_id","COALESCE(refund_amount, 0) * currency_rate as refund_amount")
          .in("platform_order_id",platformOrderIdList);
        List<DmpRefundInfoEntity> entityList = dmpRefundInfoMapper.selectList(qw);
        return entityList;
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

        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
             map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getItemList().stream().map(obj -> MathUtil.multiply(obj.getAmountAfter(),obj.getCurrencyRate())).reduce(BigDecimal.ZERO,BigDecimal::add), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getItemList().stream().map(obj -> MathUtil.multiply(obj.getAmountAfter(),obj.getCurrencyRate())).reduce(BigDecimal.ZERO,BigDecimal::add), BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
             map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                     Collectors.reducing(BigDecimal.ZERO, e -> e.getItemList().stream().map(obj -> MathUtil.multiply(obj.getAmountAfter(),obj.getCurrencyRate())).reduce(BigDecimal.ZERO,BigDecimal::add), BigDecimal::add))
            );
        }
        //根据指标查询品类目标值
        if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType())) {
            Map<String, Integer> monthMap = mainList.stream().collect(Collectors.toMap(obj -> obj.getId(), obj -> (flag ? obj.getPlatformCreateTime() : obj.getDeliveryTime()).getMonthValue()));
            map = detailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getCategoryName())).collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getCategoryName().concat(",").concat(String.valueOf(monthMap.get(x.getOrderId()))),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.multiply(e.getAmountAfter(),e.getCurrencyRate()), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())) {
            Map<String, Integer> monthMap = mainList.stream().collect(Collectors.toMap(obj -> obj.getId(), obj -> (flag ? obj.getPlatformCreateTime() : obj.getDeliveryTime()).getMonthValue()));
            //根据指标查询SKU目标值
            map = detailList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getSkuNo().concat(",").concat(String.valueOf(monthMap.get(x.getOrderId()))),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.multiply(e.getAmountAfter(),e.getCurrencyRate()), BigDecimal::add))
            );
        }
        return mapToResultList(map);
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

        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
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
                            x.getChargeName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
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
            map = detailList.stream().filter(obj -> StrUtil.isNotBlank(obj.getCategoryName())).collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getCategoryName().concat(",").concat(String.valueOf(monthMap.get(x.getOrderId()))),
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
        return mapToResultList(map);
    }


    /**
     * @description: 净销售额
     * @author Will
     * @date: 2023/9/19 11:17
     * @param mainList
     * @param refundList
     * @param dto
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> groupNetSalesAmount (List<DmpOrderInfoEntity> mainList,List<DmpRefundInfoEntity> refundList,TargetFinishDTO.ParamDTO dto) {

        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();

        Map<String, BigDecimal> map = new HashMap<>();

        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract( e.getItemList().stream().map(obj -> MathUtil.multiply(obj.getAmountAfter(),obj.getCurrencyRate())).reduce(BigDecimal.ZERO,BigDecimal::add),refundList.stream().filter(obj -> obj.getPlatformOrderId().equals(e.getPlatformOrderId())).map(DmpRefundInfoEntity::getRefundAmount).reduce(BigDecimal.ZERO,BigDecimal::add)), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract( e.getItemList().stream().map(obj -> MathUtil.multiply(obj.getAmountAfter(),obj.getCurrencyRate())).reduce(BigDecimal.ZERO,BigDecimal::add),refundList.stream().filter(obj -> obj.getPlatformOrderId().equals(e.getPlatformOrderId())).map(DmpRefundInfoEntity::getRefundAmount).reduce(BigDecimal.ZERO,BigDecimal::add)), BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            map = mainList.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf((flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract( e.getItemList().stream().map(obj -> MathUtil.multiply(obj.getAmountAfter(),obj.getCurrencyRate())).reduce(BigDecimal.ZERO,BigDecimal::add),refundList.stream().filter(obj -> obj.getPlatformOrderId().equals(e.getPlatformOrderId())).map(DmpRefundInfoEntity::getRefundAmount).reduce(BigDecimal.ZERO,BigDecimal::add)), BigDecimal::add))
            );
        }
        return mapToResultList(map);
    }

    /**
     * @description: 财务销售额
     * @author Will
     * @date: 2023/9/19 14:32
     * @param dto
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> groupOrderFinanceSalesAmount (TargetFinishDTO.ParamDTO dto) {

        Map<String, BigDecimal> map = new HashMap<>();

        BiDataSourceCostDTO.GroupDTO groupDTO = BeanMapperUtils.map(BiDataSourceCostDTO.GroupDTO.class, dto);
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome"));
        groupDTO.setCostTypeList(dictValues);
        List<BiDataSourceCostDTO.ListDTO> list = biDataSourceCostService.listBiDataSourceCost(groupDTO);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO), BigDecimal::add))
            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO), BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO), BigDecimal::add))
            );
        }
        return mapToResultList(map);
    }

    /**
     * @description: 销售毛利润
     * @author Will
     * @date: 2023/9/19 14:32
     * @param dto
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> groupOrderSalesProfit (TargetFinishDTO.ParamDTO dto) {

        Map<String, BigDecimal> map = new HashMap<>();

        BiDataSourceCostDTO.GroupDTO groupDTO = BeanMapperUtils.map(BiDataSourceCostDTO.GroupDTO.class, dto);
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost"));
        groupDTO.setCostTypeList(dictValues);
        List<BiDataSourceCostDTO.ListDTO> list = biDataSourceCostService.listBiDataSourceCost(groupDTO);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract(e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO),e.getMap().getOrDefault("cost_totalCost",BigDecimal.ZERO)) , BigDecimal::add))

            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract(e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO),e.getMap().getOrDefault("cost_totalCost",BigDecimal.ZERO)) , BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract(e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO),e.getMap().getOrDefault("cost_totalCost",BigDecimal.ZERO)) , BigDecimal::add))
            );
        }
        return mapToResultList(map);
    }

    /**
     * @description: 销售毛利率
     * @author Will
     * @date: 2023/9/19 14:32
     * @param dto
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> groupOrderSalesRatio (TargetFinishDTO.ParamDTO dto) {

        Map<String, BigDecimal> map = new HashMap<>();

        BiDataSourceCostDTO.GroupDTO groupDTO = BeanMapperUtils.map(BiDataSourceCostDTO.GroupDTO.class, dto);
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost"));
        groupDTO.setCostTypeList(dictValues);
        List<BiDataSourceCostDTO.ListDTO> list = biDataSourceCostService.listBiDataSourceCost(groupDTO);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.divide(MathUtil.subtract(e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO),e.getMap().getOrDefault("cost_totalCost",BigDecimal.ZERO)),e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO))  , BigDecimal::add))

            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.divide(MathUtil.subtract(e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO),e.getMap().getOrDefault("cost_totalCost",BigDecimal.ZERO)),e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO))  , BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.divide(MathUtil.subtract(e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO),e.getMap().getOrDefault("cost_totalCost",BigDecimal.ZERO)),e.getMap().getOrDefault("cost_mainBusinessIncome",BigDecimal.ZERO))  , BigDecimal::add))
            );
        }
        return mapToResultList(map);
    }

    /**
     * @description: map转结果集
     * @author Will
     * @date: 2023/9/19 14:51
     * @param map
     * @return List<ViewDTO>
     */
    private List<TargetFinishDTO.ViewDTO> mapToResultList (Map<String, BigDecimal> map) {
        if (ObjectUtils.isEmpty(map)) {
            return Collections.EMPTY_LIST;
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
