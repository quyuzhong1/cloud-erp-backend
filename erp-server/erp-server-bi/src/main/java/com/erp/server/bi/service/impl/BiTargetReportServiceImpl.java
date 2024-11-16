package com.erp.server.bi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.service.impl.RedisService;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.enums.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.BiOrderInfoMapper;
import com.erp.server.bi.mapper.BiRefundInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private BiRefundInfoMapper biRefundInfoMapper;

    @Resource
    private BiOrderInfoMapper biOrderInfoMapper;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private BiDataSourceCostService biDataSourceCostService;

    @Resource
    private RedisService redisService;
    
    public static final String TYPE_NAME = "typeName";

    @Override
    @Cacheable(cacheNames = "cache:bi:targetFinish",keyGenerator = "myKeyGenerator")
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

        //实际数据
        List<TargetFinishDTO.ViewDTO> realList = listReal(dto);


        //表头数据
        headMap.put(TYPE_NAME,TargetSearchTypeEnum.getByCode(dto.getSearchType()));
        headMap.put("totalName","累计年度目标/完成率");
        MonthEnum[] values = MonthEnum.values();
        for (MonthEnum monthEnum : values) {
            headMap.put(monthEnum.getCode(), CharSequenceUtil.format("{}年{}月",start.getYear(),monthEnum.getValue()));
        }
        resultMap.put("head",headMap);

        //部门
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            List<String> deptDataList = targetList.stream().map(TargetFinishDTO.ViewDTO::getTypeId).distinct().collect(Collectors.toList());
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
                result.put(TYPE_NAME,entry.getKey());

                TargetFinishDTO.TotalSlotDTO totalSlotDTO = new TargetFinishDTO.TotalSlotDTO();
                //年目标
                BigDecimal yearTotalTarget = value.stream().map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                totalSlotDTO.setYearTotalTarget(yearTotalTarget);
                //年实际
                BigDecimal yearTotalReal = realList.stream().filter(obj -> StringUtils.isNotBlank(obj.getTypeName())
                        && obj.getTypeName().equals(entry.getKey())).map(TargetFinishDTO.ViewDTO::getValue).
                        reduce(BigDecimal.ZERO, BigDecimal::add);
                totalSlotDTO.setYearTotalReal(yearTotalReal);
                BigDecimal yearRate = MathUtil.divide(yearTotalReal,yearTotalTarget);
                totalSlotDTO.setRate(MathUtil.multiply(yearRate,MathUtil.BigDecimal_100));
                result.put("totalName",totalSlotDTO);
                for (MonthEnum monthEnum : values) {
                    TargetFinishDTO.SlotDTO slotDTO = new TargetFinishDTO.SlotDTO();
                    //目标值
                    BigDecimal targetValue = value.stream().filter(obj -> Objects.equals(obj.getMonth(), monthEnum.getValue())).map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    slotDTO.setTargetValue(targetValue);
                    //实际值
                    BigDecimal realValue = realList.stream().filter(obj -> Objects.equals(obj.getMonth(), monthEnum.getValue()) && CharSequenceUtil.equals(obj.getTypeName(),entry.getKey())).map(TargetFinishDTO.ViewDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    slotDTO.setValue(realValue);
                    BigDecimal rate;
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
        }
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
            headList.add(CharSequenceUtil.format("{}年{}月",year,monthEnum.getValue()));
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
                targetMap.put(TYPE_NAME,map.get(TYPE_NAME));
                //指标
                targetMap.put("metrics","目标值");
                //年累计
                targetMap.put("yearTotal",totalSlotDTO.getYearTotalTarget().stripTrailingZeros().toPlainString());

                //实际数据
                LinkedHashMap<String, Object> realMap = new LinkedHashMap<>();
                //类型名称
                realMap.put(TYPE_NAME,map.get(TYPE_NAME));
                //指标
                realMap.put("metrics",MetricsEnum.getNameByCode(dto.getMetrics()));
                //年累计
                realMap.put("yearTotal",totalSlotDTO.getYearTotalReal().stripTrailingZeros().toPlainString());

                //完成率/占比
                LinkedHashMap<String, Object> rateMap = new LinkedHashMap<>();
                //类型名称
                rateMap.put(TYPE_NAME,map.get(TYPE_NAME));
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

        Map<String, String> dataMap = new HashMap<>();
        //部门和负责人 填充名称
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())){
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            if (CollectionUtils.isNotEmpty(userList)){
                dataMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));
            }
        }else if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())){
            List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
            if (CollectionUtils.isNotEmpty(deptList)){
                dataMap = deptList.stream().collect(Collectors.toMap(SysDepartmentDTO::getId, SysDepartmentDTO::getName));
            }
        }

        //销售额
        if (MetricsEnum.SALES_AMOUNT.getCode().equals(dto.getMetrics())) {
            resultList = biOrderInfoMapper.listSalesBiFilter(dto, "sales");
        }
        //销量
        if (MetricsEnum.SALES_QTY.getCode().equals(dto.getMetrics())) {
            resultList = biOrderInfoMapper.listSalesBiFilter(dto, "qty");
        }
        //净销售额
        if (MetricsEnum.NET_SALES_AMOUNT.getCode().equals(dto.getMetrics())) {
            //部门、品类和SKU无需显示（现马帮数据退款明细无金额暂不计算财务销售额·6）
            if (TargetSearchTypeEnum.CATEGORY.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SKU.getCode().equals(dto.getSearchType())
                    || TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
                return resultList;
            }
            dto.setCategory(null);
            dto.setSku(null);
            dto.setBrand(null);
            dto.setDepartment(null);
            resultList = biOrderInfoMapper.listSalesBiFilter(dto, "sales");
            if (CollectionUtils.isNotEmpty(resultList)) {

                TargetFinishDTO.GroupViewDTO refundGroupViewDTO = handleRefundGroupData(dto);
                //退款信息
                List<TargetFinishDTO.ViewDTO> refundList = biOrderInfoMapper.listRefundBiFilter(dto, refundGroupViewDTO);
                for (TargetFinishDTO.ViewDTO viewDTO : resultList) {
                    //部门和负责人 填充名称
                    if (StringUtils.isNotBlank(viewDTO.getTypeId()) && StringUtils.isBlank(viewDTO.getTypeName())){
                        viewDTO.setTypeName(dataMap.get(viewDTO.getTypeId()));
                    }
                    //退款金额
                    BigDecimal refundAmount = refundList.stream().filter(obj -> obj.getMonth().equals(viewDTO.getMonth()) && obj.getTypeName().equals(viewDTO.getTypeName())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse(BigDecimal.ZERO);
                    viewDTO.setValue(MathUtil.subtract(viewDTO.getValue(),refundAmount));

                }
            }
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
        if (CollectionUtils.isNotEmpty(resultList)){
            Map<String, String> finalDataMap = dataMap;
            resultList.forEach(viewDTO -> {
                if (StringUtils.isBlank(viewDTO.getTypeName()) && StringUtils.isNotBlank(viewDTO.getTypeId())){
                    viewDTO.setTypeName(finalDataMap.get(viewDTO.getTypeId()));
                }
            });
        }
        return resultList;
    }

    /**
     * @description: 退款分组获得实际数据
     * @author Will
     * @date: 2023/10/8 14:38
     * @param dto
     * @return GroupViewDTO
     */
    private TargetFinishDTO.GroupViewDTO handleRefundGroupData (TargetFinishDTO.ParamDTO dto) {

        String timeGroupStr = "to_char(dri.refund_time,'MM')";
        String timeViewStr = "to_char(dri.refund_time,'MM') as month";

        String viewStr = "";
        String groupStr = "";

        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            viewStr = "dri.charge_name as typeName,".concat(timeViewStr);
            groupStr = "dri.charge_name,".concat(timeGroupStr);
        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            viewStr = "dri.shop_name as typeName,".concat(timeViewStr);
            groupStr = "dri.shop_name,".concat(timeGroupStr);
        }
        return new TargetFinishDTO.GroupViewDTO(groupStr,viewStr);
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
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode()));
        groupDTO.setCostTypeList(dictValues);
        List<BiDataSourceCostDTO.ListDTO> list = biDataSourceCostService.listBiDataSourceCost(groupDTO);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO), BigDecimal::add))
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
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), DataSourceCostEnum.COST_TOTALCOST.getCode()));
        groupDTO.setCostTypeList(dictValues);
        List<BiDataSourceCostDTO.ListDTO> list = biDataSourceCostService.listBiDataSourceCost(groupDTO);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract(e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO),e.getMap().getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(),BigDecimal.ZERO)) , BigDecimal::add))

            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract(e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO),e.getMap().getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(),BigDecimal.ZERO)) , BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.subtract(e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO),e.getMap().getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(),BigDecimal.ZERO)) , BigDecimal::add))
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
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), DataSourceCostEnum.COST_TOTALCOST.getCode()));
        groupDTO.setCostTypeList(dictValues);
        List<BiDataSourceCostDTO.ListDTO> list = biDataSourceCostService.listBiDataSourceCost(groupDTO);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        if (TargetSearchTypeEnum.FIRST_LEVEL_DEPT.getCode().equals(dto.getSearchType()) || TargetSearchTypeEnum.SECOND_LEVEL_DEPT.getCode().equals(dto.getSearchType())) {
            //根据指标查询部门目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getDeptName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.divide(MathUtil.subtract(e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO),e.getMap().getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(),BigDecimal.ZERO)),e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO))  , BigDecimal::add))

            );
        }
        if (TargetSearchTypeEnum.USER.getCode().equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getChargeName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.divide(MathUtil.subtract(e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO),e.getMap().getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(),BigDecimal.ZERO)),e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO))  , BigDecimal::add))
            );

        }
        if (TargetSearchTypeEnum.SHOP.getCode().equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            map = list.stream().collect(Collectors.groupingBy(x ->
                            // 按照月分组
                            x.getShopName().concat(",").concat(String.valueOf(x.getMonth().getMonthValue())),
                    Collectors.reducing(BigDecimal.ZERO, e -> MathUtil.divide(MathUtil.subtract(e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO),e.getMap().getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(),BigDecimal.ZERO)),e.getMap().getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),BigDecimal.ZERO))  , BigDecimal::add))
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
            return Collections.emptyList();
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

}
