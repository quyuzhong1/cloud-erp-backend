package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.*;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCostEntity;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.BiDataSourceCostEnum;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.listener.BiDataSourceCostExcelListener;
import com.erp.server.bi.mapper.BiDataSourceCostMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:37
 */
@Service
public class BiDataSourceCostServiceImpl extends ServiceImpl<BiDataSourceCostMapper, BiDataSourceCostEntity>
        implements BiDataSourceCostService {

    @Resource
    private BiDataSourceCostDetailService biDataSourceCostDetailService;

    @Resource
    private BiDictService biDictService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCostSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BiDataSourceCostSearchDTO params = dto.getParams();
        IPage<LinkedHashMap<String,Object>> pageData = baseMapper.paging(query, params);
        LinkedHashMap<String,Object> resultMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();
        renewBiDataSourceCost(pageData.getRecords(),headMap);
        resultMap.put("head",headMap);
        resultMap.put("data",pageData.getRecords());
        pageData.setRecords(Arrays.asList(resultMap));
        return new PagingVO(pageData);
    }

    @Override
    public TargetSaleSumVO sumSalesProfit(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost", "cost_saleExpenses"));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollectionUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            // 如果主营收入小于0 数据异常 按照0 计算结果
            if (costMainBusinessIncome.compareTo(BigDecimal.ZERO) <= 0){
                return BigDecimal.ZERO;
            }
            return costMainBusinessIncome
                    .subtract(tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO))
                    .subtract(tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO));
        }));
        // 对每条数据计算结果进行累加
        BigDecimal resultAmount = detailListMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TargetSaleSumVO(resultAmount);
    }

    private List<BiDataSourceCostEntity> getCostList(BiFilterDTO dto) {
        // 查询最新月份数据
        BiDataSourceCostEntity maxMonthEntity = getMaxMonth();
        if (Objects.isNull(maxMonthEntity)) {
            return new ArrayList<>();
        }
        List<BiDataSourceCostEntity> dataSourceCostList = lambdaQuery()
                .in(CollectionUtil.isNotEmpty(dto.getSite()), BiDataSourceCostEntity::getSite, dto.getSite())
                .in(CollectionUtil.isNotEmpty(dto.getShopName()), BiDataSourceCostEntity::getShopName, dto.getShopName())
                .in(CollectionUtil.isNotEmpty(dto.getDepartment()), BiDataSourceCostEntity::getDeptName, dto.getDepartment())
                .in(CollectionUtil.isNotEmpty(dto.getPlatform()), BiDataSourceCostEntity::getPlatformName, dto.getPlatform())
                .in(CollectionUtil.isNotEmpty(dto.getShopName()), BiDataSourceCostEntity::getShopName, dto.getShopName())
                .in(CollectionUtil.isNotEmpty(dto.getUserId()), BiDataSourceCostEntity::getChargeId, dto.getUserId())
                .eq(BiDataSourceCostEntity::getMonth, maxMonthEntity.getMonth())
                .last(StrUtil.isNotBlank(dto.getParam()), dto.getParam())
                .list();
        if (CollectionUtils.isEmpty(dataSourceCostList)) {
            return new ArrayList<>();
        }

        return dataSourceCostList;
    }

    @Override
    public TargetSaleSumVO sumSalesRatio(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost", "cost_saleExpenses"));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollectionUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            // 如果主营收入小于0 数据异常 按照0 计算结果
            if (costMainBusinessIncome.compareTo(BigDecimal.ZERO) <= 0){
                return BigDecimal.ZERO;
            }
            return costMainBusinessIncome
                    .subtract(tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO))
                    .subtract(tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO))
                    .divide(costMainBusinessIncome, 2, BigDecimal.ROUND_HALF_UP);
        }));
        // 对每条数据计算结果进行累加
        BigDecimal resultAmount = detailListMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TargetSaleSumVO(resultAmount);
    }

    @Override
    public TargetSaleSumVO sumMainRevenue(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome"));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollectionUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            // 如果主营收入小于0 数据异常 按照0 计算结果
            if (costMainBusinessIncome.compareTo(BigDecimal.ZERO) <= 0){
                return BigDecimal.ZERO;
            }
            return costMainBusinessIncome;
        }));
        // 对每条数据计算结果进行累加
        BigDecimal resultAmount = detailListMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TargetSaleSumVO(resultAmount);
    }

    @Override
    public TargetSaleSumVO sumSalesCost(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_saleExpenses"));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollectionUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            // 如果主营收入小于0 数据异常 按照0 计算结果
            if (costMainBusinessIncome.compareTo(BigDecimal.ZERO) <= 0){
                return BigDecimal.ZERO;
            }
            return costMainBusinessIncome;
        }));
        // 对每条数据计算结果进行累加
        BigDecimal resultAmount = detailListMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TargetSaleSumVO(resultAmount);
    }

    @Override
    public void exportExcel(BiDataSourceCostSearchDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<LinkedHashMap<String,Object>>  list = baseMapper.getAllBiDataSourceCost(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //表头信息
        LinkedHashMap<String, Object> heads = new LinkedHashMap<>();
        List<LinkedHashMap<String, Object>> costList = renewBiDataSourceCost(list,heads);
        if (CollectionUtils.isEmpty(costList)) {
            return;
        }
        list.stream().forEach(obj ->obj.remove("id"));
        List<String> headList = new ArrayList<>();
        for (Map.Entry<String,Object> map:heads.entrySet()) {
            String value = map.getValue().toString();
            headList.add(value);
        }
        String head = "成本数据表";
        String fileName = dmpOrderInfoService.getFileName("成本数据表导出")+ ".xlsx";
        ExcelUtil.easyUtilStr(headList,head,list,fileName, response);
        return;
    }

    @Override
    @Transactional
    public void updateBiDataSourceCost(List<LinkedHashMap<String, Object>> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<BiDataSourceCostDetailEntity> updateList= new ArrayList<>();
        List<BiDataSourceCostDetailEntity> saveList= new ArrayList<>();
        for (LinkedHashMap<String, Object> map: list) {
            String id = map.get("id").toString();
            List<BiDataSourceCostDetailEntity> biDataSourceCostDetailList = biDataSourceCostDetailService.listByCostIds(Arrays.asList(id));
            Iterator<Map.Entry<String, Object>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
            if (ObjectUtils.isNotEmpty(iterator)) {
                while (iterator.hasNext()) {
                    Map.Entry entry = (java.util.Map.Entry) iterator.next();
                    if (ObjectUtils.isEmpty(entry.getKey()) || "id".equals(entry.getKey())) {
                        continue;
                    }
                    BiDataSourceCostDetailEntity detailEntity = new BiDataSourceCostDetailEntity();
                    String key = entry.getKey().toString();
                    String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue().toString();
                    detailEntity.setCostType(key);
                    detailEntity.setCostId(id);
                    if (StrUtils.isDigit(value)) {
                        BigDecimal costValue = MathUtil.valueOf(value);
                        detailEntity.setCostValue(costValue);
                        detailEntity.setValueType(MathUtil.ZERO);
                    }
                    if (StrUtils.isPercentage(value)) {
                        String costValue = value.replace("%", "");
                        detailEntity.setCostValue(MathUtil.divide(MathUtil.valueOf(costValue), new BigDecimal(100), 4));
                        detailEntity.setValueType(MathUtil.ONE);
                    }
                    if (CollectionUtils.isNotEmpty(biDataSourceCostDetailList)) {
                        BiDataSourceCostDetailEntity detail = biDataSourceCostDetailList.stream().filter(e -> e.getCostType().equals(key)).findFirst().orElse(null);
                        if (ObjectUtils.isNotEmpty(detail)) {
                            detailEntity.setId(detail.getId());
                            updateList.add(detailEntity);
                        } else {
                            saveList.add(detailEntity);
                        }
                    } else {
                        saveList.add(detailEntity);
                    }
                }
            }
        }
        //更新明细数据
        if (CollectionUtils.isNotEmpty(updateList)) {
            biDataSourceCostDetailService.updateBatchById(updateList);
        }
        //明细不存在时新增明细数据
       if (CollectionUtils.isNotEmpty(saveList)) {
           biDataSourceCostDetailService.saveBatch(saveList);
       }
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {

        //查询店铺数据
        List<DmpShopInfoEntity> shopList = dmpShopInfoService.list();
        //查人员数据
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //查询成本字典数据
        List<BiDictEntity> dictList = biDictService.listEntityByType(DictEnum.DATASOURCECOST.getType());

        BiDataSourceCostExcelListener excelListenerUtil = new BiDataSourceCostExcelListener(this,biDataSourceCostDetailService,shopList,userList,dictList);
        try {
            EasyExcel.read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
            List<Map<Integer, String>> list = excelListenerUtil.getDateList();
            if (CollectionUtils.isEmpty(list) || list.size() == 0) {
                return true;
            }
            List<String> headList = excelListenerUtil.getHead();
            String head = "成本数据表";
            String fileName = dmpOrderInfoService.getFileName("成本数据表导出")+ ".xlsx";
            ExcelUtil.easyUtil(headList,head,list,fileName, response);
        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }
        return false;
    }


    /**
     * 返回字段处理
     */
    private List<LinkedHashMap<String,Object>> renewBiDataSourceCost(List<LinkedHashMap<String,Object>> list,LinkedHashMap<String,Object> head) {

        //返回中文类型的数据
        List<LinkedHashMap<String,Object>> cnResultMap = new ArrayList<>();

        //查询成本字典数据
        List<BiDictEntity> dictList = biDictService.listEntityByType(DictEnum.DATASOURCECOST.getType());

        List<BiDataSourceCostDetailEntity> biDataSourceCostDetailList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            //查询明细数据
            List<String> costIds = list.stream().map((Map m) -> (String) m.get("id")).collect(Collectors.toList());
            biDataSourceCostDetailList= biDataSourceCostDetailService.listByCostIds(costIds);
        }
        BiDataSourceCostEnum[] values = BiDataSourceCostEnum.values();
        //新增固定表头
        for (BiDataSourceCostEnum value:values) {
            if (ObjectUtils.isEmpty(head.get(value.getCode()))) {
                head.put(value.getCode(),value.getName());
            }
        }
        if (CollectionUtils.isNotEmpty(dictList)) {
            //新增变动表头
            for (BiDictEntity dcit : dictList) {
                if (ObjectUtils.isEmpty(head.get(dcit.getValue()))) {
                    head.put(dcit.getValue(),dcit.getName());
                }
            }
        }
        //数据处理
        for (LinkedHashMap<String,Object> map: list) {
            LinkedHashMap<String,Object> cnMap = new LinkedHashMap<>();

            for (BiDataSourceCostEnum value:values) {
                cnMap.put(value.getName(),map.get(value.getCode()));
            }
            if (CollectionUtils.isNotEmpty(dictList)) {
                for (BiDictEntity dcit : dictList) {
                    BigDecimal value = BigDecimal.ZERO;
                    if (CollectionUtils.isNotEmpty(biDataSourceCostDetailList)) {
                         value = biDataSourceCostDetailList.stream().filter(obj -> obj.getCostId().equals(map.get("id")) && obj.getCostType().equals(dcit.getValue()))
                                .map(BiDataSourceCostDetailEntity::getCostValue).findFirst().orElse(BigDecimal.ZERO);
                    }
                    map.put(dcit.getValue(),value);
                    cnMap.put(dcit.getName(),value);
                }
            }
            cnMap.remove("id");
            cnResultMap.add(map);
        }
        return cnResultMap;
    }

    @Override
    public BiDataSourceCostEntity getByCostParam(BiDataSourceCostEntity entity) {
        LambdaQueryWrapper<BiDataSourceCostEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDataSourceCostEntity::getShopName,entity.getShopName());
        queryWrapper.eq(BiDataSourceCostEntity::getPlatformName,entity.getPlatformName());
        queryWrapper.eq(BiDataSourceCostEntity::getSite,entity.getSite());
        queryWrapper.eq(BiDataSourceCostEntity::getMonth,entity.getMonth());
        queryWrapper.last("limit 1");
        return  this.getOne(queryWrapper);
    }

    @Override
    public List<SeriesVO> getDeptCostProfit(BiFilterDTO dto) {
        // 统计成本数据
        List<DeptCostVO> deptCostVOS = this.sumCostByCondition(dto,"dept_name");
        if (CollectionUtil.isEmpty(deptCostVOS)){
            return getDeptSeriesVo(null, null);
        }
        // 销售毛利润
        Map<String, Map<String, BigDecimal>> costMap = deptCostVOS.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));

        // 成本
        List<DeptCostVO> costSaleExpenses = deptCostVOS.stream()
                .filter(x -> "cost_saleExpenses".equals(x.getCostType()))
                .sorted(Comparator.comparing(DeptCostVO::getCostValue))
                .collect(Collectors.toList());
        // 表头 SeriesVO
        List<SeriesVO> seriesList = getDeptSeriesVo(profitMap, costSaleExpenses);
        return seriesList;
    }

    private static List<SeriesVO> getDeptSeriesVo(Map<String, BigDecimal> profitMap, List<DeptCostVO> costSaleExpenses) {
        List<SeriesVO> seriesList = new ArrayList<>();
        SeriesVO costVo = new SeriesVO<>();
        costVo.setName("成本");
        List<BigDecimal> costList = Collections.emptyList();
        if (CollectionUtil.isNotEmpty(costSaleExpenses)){
            costList = costSaleExpenses.stream().map(DeptCostVO::getCostValue).collect(Collectors.toList());
        }
        costVo.setData(costList);
        seriesList.add(costVo);
        SeriesVO profitVo = new SeriesVO<>();
        profitVo.setName("毛利润");
        List<BigDecimal> profitList = Collections.emptyList();
        if (CollectionUtil.isNotEmpty(costSaleExpenses)){
            profitList = costSaleExpenses.stream().map(x -> profitMap.get(x.getName())).collect(Collectors.toList());
        }
        profitVo.setData(profitList);
        seriesList.add(profitVo);
        SeriesVO yAxis = new SeriesVO<>();
        yAxis.setName("名称");
        List<String> deptNames = Collections.emptyList();
        if (CollectionUtil.isNotEmpty(costSaleExpenses)){
            deptNames = costSaleExpenses.stream().map(DeptCostVO::getName).collect(Collectors.toList());
        }
        yAxis.setData(deptNames);
        seriesList.add(yAxis);
        return seriesList;
    }



    @Override
    public BiDataSourceCostEntity getMaxMonth() {
        QueryWrapper<BiDataSourceCostEntity> queryWrapper = new QueryWrapper();
        queryWrapper.select("max(month) as month");
        queryWrapper.last("limit 1");
        BiDataSourceCostEntity maxMonthEntity = baseMapper.selectOne(queryWrapper);
        return maxMonthEntity;
    }

    @Override
    public List<DeptCostVO> sumCostByCondition(BiFilterDTO dto, String groupName) {
        BiDataSourceCostEntity entity = getMaxMonth();
        LocalDateTime month = entity.getMonth();
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost", "cost_saleExpenses"));
        List<DeptCostVO> vo = baseMapper.sumByDeptAndCostType(month, dto, dictValues, groupName);
        if(CollectionUtil.isNotEmpty(vo)){
            vo.stream().peek(x -> x.setMonth(month)).collect(Collectors.toList());
        }
        return vo;
    }

    @Override
    public List<SeriesVO> getShopCostProfit(BiFilterDTO dto) {
        // 查询成本数据
        List<DeptCostVO> shopCostVos = sumCostByCondition(dto, "shop_name");
        if (CollectionUtil.isEmpty(shopCostVos)){
            return getSeriesVOS(null);
        }
        // 销售毛利率
        Map<String, Map<String, BigDecimal>> costMap = shopCostVos.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));
        Map<String, BigDecimal> result = MapUtil.sortByValue(profitMap, true);
        // 表头 SeriesVO
        List<SeriesVO> seriesList = getSeriesVOS(result);
        return seriesList;
    }

    @Override
    public List<SeriesVO> getPlatformCostProfit(BiFilterDTO dto) {
        // 查询成本数据
        List<DeptCostVO> shopCostVos = sumCostByCondition(dto, "platform_name");
        if (CollectionUtil.isEmpty(shopCostVos)){
            return getSeriesVOS(null);
        }
        // 销售毛利率
        Map<String, Map<String, BigDecimal>> costMap = shopCostVos.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));
        Map<String, BigDecimal> sortMap = MapUtil.sortByValue(profitMap, false);
        Integer rankNum;
        if (profitMap.size() <= 5) {
            rankNum = profitMap.size();
        }else {
            rankNum = new BigDecimal(profitMap.keySet().size())
                    .multiply(new BigDecimal("0.2"))
                    .setScale(0, BigDecimal.ROUND_UP).intValue();
        }
        Map<String, BigDecimal> result = sortMap.entrySet().stream()
                .limit(rankNum)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, BigDecimal> descSortMap = MapUtil.sortByValue(result, true);
        // 表头 SeriesVO
        List<SeriesVO> seriesList = getSeriesVOS(descSortMap);
        return seriesList;
    }

    @Override
    public List<PieChartVO> getPlatformCostPercent(BiFilterDTO dto) {
        // 查询成本数据
        List<DeptCostVO> shopCostVos =sumCostByCondition(dto, "platform_name");
        if (CollectionUtil.isEmpty(shopCostVos)){
            return Collections.emptyList();
        }
        // 销售毛利润
        Map<String, Map<String, BigDecimal>> costMap = shopCostVos.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));
        BigDecimal totalAmount = profitMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<PieChartVO> result = profitMap.entrySet().stream()
                .map(x -> new PieChartVO(x.getKey(), x.getValue(), totalAmount))
                .collect(Collectors.toList());
        // 重新计算把多出来的百分比加到最后一位
        BigDecimal totalPercent = BigDecimal.ZERO;
        for (int i = 0; i < result.size(); i++) {
            BigDecimal percent = result.get(i).getPercent();
            if(i == result.size() - 1){
                percent = new BigDecimal(100).subtract(totalPercent);
                result.get(i).setPercent(percent);
            }else {
                totalPercent = totalPercent.add(percent);
            }
        }

        return result;
    }

    @Override
    public List<SeriesVO> getMonthCostProfit(BiFilterDTO dto) {
        // 成本 利润
        // 统计成本数据
        List<DateCostVO> dateCostVOS = this.sumCostByDate(dto, 0);
        if (CollectionUtil.isEmpty(dateCostVOS)){
            return getDateAnalyze(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 12);
        }
        // 销售毛利率
        Map<LocalDate, Map<String, BigDecimal>> costMap = dateCostVOS.stream()
                .collect(Collectors.groupingBy(DateCostVO::getGroupDate,
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));
        Map<Integer, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e.getMonthValue(), v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));

        // 成本
        List<DateCostVO> costSaleExpenses = dateCostVOS.stream()
                .filter(x -> "cost_saleExpenses".equals(x.getCostType()))
                .sorted(Comparator.comparing(DateCostVO::getGroupDate))
                .collect(Collectors.toList());
        // 月度成本汇总
        Map<Integer, BigDecimal> monthCostMap = costSaleExpenses.stream()
                .collect(Collectors.toMap(x -> x.getGroupDate().getMonthValue(), DateCostVO::getCostValue));
        // 时间分组销售额-月
        Map<Integer, BigDecimal> monthSalesMap = dmpOrderInfoService.statisticsSalesByDate(dto, 0);

        // 净利润
        HashMap<Integer, BigDecimal> netProfitMap = new HashMap<>();
        // 表头 SeriesVO
        List<SeriesVO> seriesList = getDateAnalyze(profitMap, monthCostMap, monthSalesMap, netProfitMap, 12);
        return seriesList;
    }

    /**
     * 日期数据分析
     *
     * @param profitMap
     * @param costMap
     * @param salesMap
     * @param netProfitMap
     * @param dateType  12 月 4 季度  0 年
     * @return
     */
    private List<SeriesVO> getDateAnalyze(Map<Integer, BigDecimal> profitMap, Map<Integer, BigDecimal> costMap, Map<Integer, BigDecimal> salesMap,
                                          HashMap<Integer, BigDecimal> netProfitMap, Integer dateType) {
        List<SeriesVO> seriesList = new ArrayList<>();
        SeriesVO xAxis = new SeriesVO<>();
        xAxis.setName("名称");
        String format;
        Integer startRange = 1;
        Integer endRange = dateType;
        if(12 == dateType){
            format = "{}月";
        }else if (4 == dateType){
            format = "Q{}";
        } else {
            format = "{}年";
            Set<Integer> year1 = new HashSet<>(profitMap.keySet());
            Set<Integer> year2 = new HashSet<>(salesMap.keySet());
            year1.addAll(year2);
            startRange = year1.stream().min(Integer::compareTo).get();
            endRange = year1.stream().max(Integer::compareTo).get();
        }
        List<String> deptNames = IntStream.rangeClosed(startRange, endRange).mapToObj(x -> StrUtil.format(format, x)).collect(Collectors.toList());

        xAxis.setData(deptNames);
        seriesList.add(xAxis);

        // 必须包含12 个月
        SeriesVO salesVo = new SeriesVO<>();
        salesVo.setName("销售额");
        List<String> salesList = IntStream.rangeClosed(startRange, endRange).mapToObj(x ->
                        salesMap.getOrDefault(x, BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString())
                    .collect(Collectors.toList());
        salesVo.setData(salesList);
        seriesList.add(salesVo);

        SeriesVO costVo = new SeriesVO<>();
        costVo.setName("成本");
        List<String> costList = IntStream.rangeClosed(startRange, endRange).mapToObj(x ->
                        costMap.getOrDefault(x, BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString())
                    .collect(Collectors.toList());
        costVo.setData(costList);
        seriesList.add(costVo);

        SeriesVO profitVo = new SeriesVO<>();
        profitVo.setName("毛利润");
        List<String> profitList = IntStream.rangeClosed(startRange, endRange).mapToObj(x ->
                        profitMap.getOrDefault(x, BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString())
                    .collect(Collectors.toList());
        profitVo.setData(profitList);
        seriesList.add(profitVo);

        SeriesVO netProfitVo = new SeriesVO<>();
        netProfitVo.setName("净利率");
        List<String> netProfitList =IntStream.rangeClosed(startRange, endRange).mapToObj(x ->
                        netProfitMap.getOrDefault(x,BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString())
                    .collect(Collectors.toList());

        netProfitVo.setData(netProfitList);
        seriesList.add(netProfitVo);

        return seriesList;

    }

    @Override
    public List<SeriesVO> getQuarterCostProfit(BiFilterDTO dto) {
        // 成本 利润
        // 统计成本数据
        List<DateCostVO> dateCostVOS = this.sumCostByDate(dto, 0);
        if (CollectionUtil.isEmpty(dateCostVOS)){
            return getDateAnalyze(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 4);
        }
        // 销售毛利率 按照季度汇总数据
        Map<LocalDate, Map<String, BigDecimal>> costMap = dateCostVOS.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate(),
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));
        // 月度数据
        Map<Integer, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e.getMonthValue(), v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));
        // 季度数据  (x- 1) / 3 + 1
        Map<Integer, BigDecimal> quarterMap = profitMap.entrySet().stream()
                .collect(Collectors.groupingBy(x -> (x.getKey() - 1) / 3 + 1,
                        BigDecimalUtil.summingBigDecimal(Map.Entry::getValue)));

        // 成本
        List<DateCostVO> costSaleExpenses = dateCostVOS.stream()
                .filter(x -> "cost_saleExpenses".equals(x.getCostType()))
                .sorted(Comparator.comparing(DateCostVO::getGroupDate))
                .collect(Collectors.toList());
        // 季度成本汇总
        Map<Integer, BigDecimal> quarterCostMap = costSaleExpenses.stream()
                .collect(Collectors.groupingBy(x -> (x.getGroupDate().getMonthValue() - 1) / 3 + 1,
                        BigDecimalUtil.summingBigDecimal(DateCostVO::getCostValue)));

        // 时间分组销售额-季度
        Map<Integer, BigDecimal> quarterSalesMap = dmpOrderInfoService.statisticsSalesByDate(dto, 1);

        // 净利润 TODO
        HashMap<Integer, BigDecimal> netProfitMap = new HashMap<>(4);
        // 表头 SeriesVO
        List<SeriesVO> seriesList = getDateAnalyze(quarterMap, quarterCostMap, quarterSalesMap, netProfitMap, 4);
        return seriesList;
    }

    @Override
    public List<SeriesVO> getYearCostProfit(BiFilterDTO dto) {
        // 成本 利润
        // 统计成本数据
        List<DateCostVO> dateCostVOS = this.sumCostByDate(dto, 1);
        if (CollectionUtil.isEmpty(dateCostVOS)){
            return getDateAnalyze(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 0);
        }
        // 销售毛利率
        Map<LocalDate, Map<String, BigDecimal>> costMap = dateCostVOS.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate(),
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));
        // 年度分组数据销售毛利率
        Map<Integer, BigDecimal> yearMap = costMap.keySet().stream().collect(Collectors.groupingBy(e -> e.getYear(), BigDecimalUtil.summingBigDecimal(v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        })));

        // 成本
        List<DateCostVO> costSaleExpenses = dateCostVOS.stream()
                .filter(x -> "cost_saleExpenses".equals(x.getCostType()))
                .sorted(Comparator.comparing(DateCostVO::getGroupDate))
                .collect(Collectors.toList());
        // 季度成本汇总
        Map<Integer, BigDecimal> yearCostMap = costSaleExpenses.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate().getYear(),
                        BigDecimalUtil.summingBigDecimal(DateCostVO::getCostValue)));

        // 时间分组销售额-季度
        Map<Integer, BigDecimal> yearSalesMap = dmpOrderInfoService.statisticsSalesByDate(dto, 2);

        // 净利润 TODO
        HashMap<Integer, BigDecimal> netProfitMap = new HashMap<>(4);
        // 表头 SeriesVO
        List<SeriesVO> seriesList = getDateAnalyze(yearMap, yearCostMap, yearSalesMap, netProfitMap, 0);
        return seriesList;
    }

    @Override
    public List<CostProfitAnalyzeRankVO> getDeptCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "dept_name", "dept_name");
        return rankResult;
    }

    private List<CostProfitAnalyzeRankVO> getCostProfitAnalyzeRankVOS(BiFilterDTO dto, String groupName, String saleGroupName) {
        // 统计成本数据
        List<DeptCostVO> deptCostVOS = this.sumCostByCondition(dto,groupName);
        if (CollectionUtil.isEmpty(deptCostVOS)){
            return new ArrayList<>();
        }
        // 销售毛利润
        Map<String, Map<String, BigDecimal>> costMap = deptCostVOS.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> deptProfitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault("cost_totalCost", BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault("cost_saleExpenses", BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));

        // 销售成本
        Map<String, BigDecimal> deptCostMap = deptCostVOS.stream()
                .filter(x -> "cost_saleExpenses".equals(x.getCostType()))
                .collect(Collectors.toMap(DeptCostVO::getName, DeptCostVO::getCostValue));
        // 主营收入
        Map<String, BigDecimal> deptMainMap = deptCostVOS.stream()
                .filter(x -> "cost_mainBusinessIncome".equals(x.getCostType()))
                .collect(Collectors.toMap(DeptCostVO::getName, DeptCostVO::getCostValue));
        // 部门销售额
        dto.setStartTime(LocalDateTime.of(LocalDate.from(deptCostVOS.get(0).getMonth().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN));
        dto.setEndTime(LocalDateTime.of(LocalDate.from(deptCostVOS.get(0).getMonth().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX));
        Map<String, BigDecimal> deptSalesMap = dmpOrderInfoService.statisticsSalesByCondition(dto, saleGroupName);
        List<CostProfitAnalyzeRankVO> resultList = deptProfitMap.keySet().stream().map(x -> new CostProfitAnalyzeRankVO(x, deptProfitMap.getOrDefault(x, BigDecimal.ZERO),
                deptCostMap.getOrDefault(x, BigDecimal.ZERO), deptSalesMap.getOrDefault(x, BigDecimal.ZERO),
                deptMainMap.getOrDefault(x, BigDecimal.ZERO))).collect(Collectors.toList());
        AtomicInteger rankIndex = new AtomicInteger(1);
        List<CostProfitAnalyzeRankVO> rankResult = resultList.stream()
                .sorted(Comparator.comparing(CostProfitAnalyzeRankVO.getByRankKey(dto.getRankKey())).reversed())
                .peek(x -> x.setRanking(rankIndex.getAndIncrement()))
                .filter(x ->x.getRanking() <= dto.getRankNum())
                .collect(Collectors.toList());
        return rankResult;
    }

    @Override
    public List<CostProfitAnalyzeRankVO> getPlatformCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "platform_name", "source_platform");
        return rankResult;
    }

    @Override
    public List<CostProfitAnalyzeRankVO> getShopCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "shop_name", "shop_name");
        return rankResult;
    }

    @Override
    public List<CostProfitAnalyzeRankVO> getUserCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "charge_name", "charge_name");
        return rankResult;
    }

    /**
     * 通过日期汇总成本数据
     * @param dto
     * @param type  0 返回今年数据  1 返回所有年份数据
     * @return
     */
    private List<DateCostVO> sumCostByDate(BiFilterDTO dto, Integer type) {
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost", "cost_saleExpenses"));
        if (0 == type) {
            LocalDateTime startTime = LocalDateTime.of(LocalDate.from(LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear())), LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(LocalDate.from(LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear())), LocalTime.MAX);
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
        }else {
            dto.setStartTime(null);
            dto.setEndTime(null);
        }

        List<DateCostVO> vo = baseMapper.sumByDateAndCostType(dto, dictValues);
        return vo;
    }

    private List<SeriesVO> getDeptSeriesVo(Map<Integer, BigDecimal> profitMap, List<DateCostVO> costSaleExpenses, Map<Integer, BigDecimal> monthSalesMap) {
        List<SeriesVO> seriesList = new ArrayList<>();
        SeriesVO costVo = new SeriesVO<>();
        costVo.setName("成本");
        List<BigDecimal> costList = Collections.emptyList();
        if (CollectionUtil.isEmpty(costSaleExpenses)){
            costList = costSaleExpenses.stream().map(DateCostVO::getCostValue).collect(Collectors.toList());
        }
        costVo.setData(costList);
        seriesList.add(costVo);
        SeriesVO profitVo = new SeriesVO<>();
        profitVo.setName("毛利润");
        List<BigDecimal> profitList = Collections.emptyList();
        if (CollectionUtil.isEmpty(costSaleExpenses)){
            profitList = costSaleExpenses.stream().map(x -> profitMap.get(x.getGroupDate().getMonthValue())).collect(Collectors.toList());
        }
        profitVo.setData(profitList);
        seriesList.add(profitVo);
        SeriesVO yAxis = new SeriesVO<>();
        yAxis.setName("名称");
        List<String> deptNames = Collections.emptyList();
        if (CollectionUtil.isEmpty(costSaleExpenses)){
            deptNames = costSaleExpenses.stream()
                    .map(x -> x.getGroupDate().getMonthValue() + "月")
                    .collect(Collectors.toList());
        }
        yAxis.setData(deptNames);
        seriesList.add(yAxis);
        SeriesVO salesVo = new SeriesVO<>();
        salesVo.setName("销售额");
        List<BigDecimal> salesList = Collections.emptyList();
        if (CollectionUtil.isEmpty(salesList)){
            salesList = costSaleExpenses.stream().map(x -> monthSalesMap.get(x.getGroupDate().getMonthValue())).collect(Collectors.toList());
        }
        salesVo.setData(salesList);
        seriesList.add(salesVo);
        return seriesList;

    }

    private static List<SeriesVO> getSeriesVOS(Map<String, BigDecimal> profitMap) {
        List<SeriesVO> seriesList = new ArrayList<>();
        SeriesVO profitVo = new SeriesVO<>();
        profitVo.setName("销售利润");
        List<BigDecimal> profitList = Collections.emptyList();
        if(CollectionUtil.isNotEmpty(profitMap)){
            profitList = profitMap.keySet().stream().map(x -> profitMap.get(x))
                    .collect(Collectors.toList());
        }
        profitVo.setData(profitList);
        seriesList.add(profitVo);
        SeriesVO yAxis = new SeriesVO<>();
        yAxis.setName("名称");
        List<String> titleList = Collections.emptyList();
        if(CollectionUtil.isNotEmpty(profitMap)){
            titleList = profitMap.keySet().stream().collect(Collectors.toList());
        }
        yAxis.setData(titleList);
        seriesList.add(yAxis);
        return seriesList;
    }
}
