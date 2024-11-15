package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.business.vo.SeriesVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.CompletionRateRankingDTO;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCostEntity;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.enums.DataSourceCostEnum;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.BiDataSourceCostEnum;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.listener.BiDataSourceCostExcelListener;
import com.erp.server.bi.mapper.BiDataSourceCostMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
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

/**
 * @author Will
 * @version 1.0

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
    private BiOrderInfoService biOrderInfoService;

    @Resource
    private BiShopInfoService biShopInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCostSearchDTO> dto) {
        Page<Object> query = new Page(dto.getCurrPage(), dto.getPageSize());
        BiDataSourceCostSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
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
    public List<BiDataSourceCostDTO.ListDTO> listBiDataSourceCost (BiDataSourceCostDTO.GroupDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = dto.getCostTypeList();
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = listCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return Collections.EMPTY_LIST;
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollUtil.isEmpty(dataSourceCostDetailMap)){
            return Collections.EMPTY_LIST;
        }
        List<BiDataSourceCostDTO.ListDTO> resultList = new ArrayList<>();
        for (BiDataSourceCostEntity biDataSourceCostEntity : dataSourceCostList) {
            BiDataSourceCostDTO.ListDTO listDTO = new BiDataSourceCostDTO.ListDTO();
            BeanMapperUtils.copy(biDataSourceCostEntity,listDTO);
            Map<String, BigDecimal> map = dataSourceCostDetailMap.get(biDataSourceCostEntity.getId());
            if (ObjectUtils.isEmpty(map)) {
                continue;
            }
            listDTO.setMap(map);
            resultList.add(listDTO);
        }
        return resultList;
    }


    @Override
    @Cacheable(cacheNames = "cache:bi:sumSalesProfit",keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO sumSalesProfit(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), DataSourceCostEnum.COST_TOTALCOST.getCode(), DataSourceCostEnum.COST_SALEEXPENSES.getCode()));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            // 如果主营收入小于0 数据异常 按照0 计算结果
            if (costMainBusinessIncome.compareTo(BigDecimal.ZERO) <= 0){
                return BigDecimal.ZERO;
            }
            return costMainBusinessIncome
                    .subtract(tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO))
                    .subtract(tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO));
        }));
        // 对每条数据计算结果进行累加
        BigDecimal resultAmount = detailListMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TargetSaleSumVO(resultAmount);
    }

    /**
     * 查询成本数据
     */
    private List<BiDataSourceCostEntity> listCostList (BiDataSourceCostDTO.GroupDTO dto) {
        List<BiDataSourceCostEntity> dataSourceCostList = lambdaQuery()
                .in(CollUtil.isNotEmpty(dto.getSite()), BiDataSourceCostEntity::getSite, dto.getSite())
                .in(CollUtil.isNotEmpty(dto.getShopName()), BiDataSourceCostEntity::getShopName, dto.getShopName())
                .in(CollUtil.isNotEmpty(dto.getDepartment()), BiDataSourceCostEntity::getDeptId, dto.getDepartment())
                .in(CollUtil.isNotEmpty(dto.getPlatform()), BiDataSourceCostEntity::getPlatformName, dto.getPlatform())
                .in(CollUtil.isNotEmpty(dto.getUserId()), BiDataSourceCostEntity::getChargeId, dto.getUserId())
                .ge(BiDataSourceCostEntity::getMonth,dto.getStartTime())
                .le(BiDataSourceCostEntity::getMonth,dto.getEndTime())
                .last(StrUtil.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql())
                .list();
        return dataSourceCostList;
    }

    /**
     * 获取最新月的成本数据
     */
    private List<BiDataSourceCostEntity> getCostList(BiFilterDTO dto) {
        // 查询最新月份数据
        BiDataSourceCostEntity maxMonthEntity = getMaxMonth();
        if (Objects.isNull(maxMonthEntity)) {
            return new ArrayList<>();
        }
        List<BiDataSourceCostEntity> dataSourceCostList = lambdaQuery()
                .in(CollUtil.isNotEmpty(dto.getSite()), BiDataSourceCostEntity::getSite, dto.getSite())
                .in(CollUtil.isNotEmpty(dto.getShopName()), BiDataSourceCostEntity::getShopName, dto.getShopName())
                .in(CollUtil.isNotEmpty(dto.getDepartment()), BiDataSourceCostEntity::getDeptName, dto.getDepartment())
                .in(CollUtil.isNotEmpty(dto.getPlatform()), BiDataSourceCostEntity::getPlatformName, dto.getPlatform())
                .in(CollUtil.isNotEmpty(dto.getShopName()), BiDataSourceCostEntity::getShopName, dto.getShopName())
                .in(CollUtil.isNotEmpty(dto.getUserId()), BiDataSourceCostEntity::getChargeId, dto.getUserId())
                .eq(BiDataSourceCostEntity::getMonth, maxMonthEntity.getMonth())
                .last(StrUtil.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql())
                .list();
        if (CollectionUtils.isEmpty(dataSourceCostList)) {
            return new ArrayList<>();
        }

        return dataSourceCostList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumSalesRatio",keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO sumSalesRatio(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), DataSourceCostEnum.COST_TOTALCOST.getCode(), DataSourceCostEnum.COST_SALEEXPENSES.getCode()));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            // 如果主营收入小于0 数据异常 按照0 计算结果
            if (costMainBusinessIncome.compareTo(BigDecimal.ZERO) <= 0){
                return BigDecimal.ZERO;
            }
            return costMainBusinessIncome
                    .subtract(tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO))
                    .subtract(tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO))
                    .divide(costMainBusinessIncome, 2, BigDecimal.ROUND_HALF_UP);
        }));
        // 对每条数据计算结果进行累加
        BigDecimal resultAmount = detailListMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TargetSaleSumVO(resultAmount);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumMainRevenue",keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO sumMainRevenue(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode()));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
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
    @Cacheable(cacheNames = "cache:bi:sumSalesCost",keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO sumSalesCost(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_SALEEXPENSES.getCode()));
        // 获取成本详情ids
        List<BiDataSourceCostEntity> dataSourceCostList = getCostList(dto);
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
        if(CollUtil.isEmpty(dataSourceCostDetailMap)){
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 计算单条记录毛利率
        Map<String, BigDecimal> detailListMap = dataSourceCostDetailMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            Map<String, BigDecimal> tempMap = e.getValue();
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
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
        String fileName = biOrderInfoService.getFileName("成本数据表导出")+ ".xlsx";
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
                        if (detail != null) {
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
        List<BiShopInfoEntity> shopList = biShopInfoService.list();
        //查人员数据
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        //查询成本字典数据
        List<BiDictEntity> dictList = biDictService.listEntityByType(DictEnum.DATASOURCECOST.getType());

        BiDataSourceCostExcelListener excelListenerUtil = new BiDataSourceCostExcelListener(this,biDataSourceCostDetailService,shopList,userList,dictList,deptList);
        try {
            EasyExcel.read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
            List<Map<Integer, String>> list = excelListenerUtil.getDateList();
            if (CollectionUtils.isEmpty(list) || list.size() == 0) {
                return true;
            }
            List<String> headList = excelListenerUtil.getHead();
            String head = "成本数据表";
            String fileName = biOrderInfoService.getFileName("成本数据表导出")+ ".xlsx";
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
    @Cacheable(cacheNames = "cache:bi:getDeptCostProfit",keyGenerator = "myKeyGenerator")
    public List<SeriesVO<String>> getDeptCostProfit(BiFilterDTO dto) {
        // 统计成本数据
        List<DeptCostVO> deptCostVOS = this.sumCostByCondition(dto,"dept_name");
        if (CollUtil.isEmpty(deptCostVOS)){
            return getDeptSeriesVo(new HashMap<>(), Collections.emptyList());
        }
        // 销售毛利润
        Map<String, Map<String, BigDecimal>> costMap = deptCostVOS.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, String> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses).toString();
        }));

        // 成本
        List<DeptCostVO> costSaleExpenses = deptCostVOS.stream()
                .filter(x -> DataSourceCostEnum.COST_SALEEXPENSES.getCode().equals(x.getCostType()))
                .sorted(Comparator.comparing(DeptCostVO::getCostValue))
                .collect(Collectors.toList());
        // 表头 SeriesVO
        List<SeriesVO<String>> seriesList = getDeptSeriesVo(profitMap, costSaleExpenses);
        return seriesList;
    }

    private static List<SeriesVO<String>> getDeptSeriesVo(Map<String, String> profitMap, List<DeptCostVO> costSaleExpenses) {
        List<SeriesVO<String>> seriesList = new ArrayList<>();
        SeriesVO costVo = new SeriesVO<>();
        costVo.setName("成本");
        List<String> costList = Collections.emptyList();
        if (CollUtil.isNotEmpty(costSaleExpenses)){
            costList = costSaleExpenses.stream().map(req -> String.valueOf(req.getCostValue())).collect(Collectors.toList());
        }
        costVo.setData(costList);
        seriesList.add(costVo);
        SeriesVO<String> profitVo = new SeriesVO<>();
        profitVo.setName("毛利润");
        List<String> profitList = Collections.emptyList();
        if (CollUtil.isNotEmpty(costSaleExpenses)){
            profitList = costSaleExpenses.stream().map(x -> profitMap.get(x.getName())).collect(Collectors.toList());
        }
        profitVo.setData(profitList);
        seriesList.add(profitVo);
        SeriesVO<String> yAxis = new SeriesVO<>();
        yAxis.setName("名称");
        List<String> deptNames = Collections.emptyList();
        if (CollUtil.isNotEmpty(costSaleExpenses)){
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
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), DataSourceCostEnum.COST_TOTALCOST.getCode(), DataSourceCostEnum.COST_SALEEXPENSES.getCode()));
        List<DeptCostVO> vo = baseMapper.sumByDeptAndCostType(month, dto, dictValues, groupName);
        if(CollUtil.isNotEmpty(vo)){
            vo.stream().peek(x -> x.setMonth(month)).collect(Collectors.toList());
        }
        return vo;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getShopCostProfit",keyGenerator = "myKeyGenerator")
    public List<SeriesVO<String>> getShopCostProfit(BiFilterDTO dto) {
        // 查询成本数据
        List<DeptCostVO> shopCostVos = sumCostByCondition(dto, "shop_name");
        if (CollUtil.isEmpty(shopCostVos)){
            return getSeriesVOS(new HashMap<>());
        }
        // 销售毛利率
        Map<String, Map<String, BigDecimal>> costMap = shopCostVos.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));
        Map<String, BigDecimal> result = MapUtil.sortByValue(profitMap, false);
        // 表头 SeriesVO
        List<SeriesVO<String>> seriesList = getSeriesVOS(result);
        return seriesList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getPlatformCostProfit",keyGenerator = "myKeyGenerator")
    public List<SeriesVO<String>> getPlatformCostProfit(BiFilterDTO dto) {
        // 查询成本数据
        List<DeptCostVO> shopCostVos = sumCostByCondition(dto, "platform_name");
        if (CollUtil.isEmpty(shopCostVos)){
            return getSeriesVOS(new HashMap<>());
        }
        // 销售毛利率
        Map<String, Map<String, BigDecimal>> costMap = shopCostVos.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));
        Map<String, BigDecimal> sortMap = MapUtil.sortByValue(profitMap, true);
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
        Map<String, BigDecimal> descSortMap = MapUtil.sortByValue(result, false);
        // 表头 SeriesVO
        List<SeriesVO<String>> seriesList = getSeriesVOS(descSortMap);
        return seriesList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getPlatformCostPercent",keyGenerator = "myKeyGenerator")
    public List<PieChartVO> getPlatformCostPercent(BiFilterDTO dto) {
        // 查询成本数据
        List<DeptCostVO> shopCostVos =sumCostByCondition(dto, "platform_name");
        if (CollUtil.isEmpty(shopCostVos)){
            return Collections.emptyList();
        }
        // 销售毛利润
        Map<String, Map<String, BigDecimal>> costMap = shopCostVos.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
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
    @Cacheable(cacheNames = "cache:bi:getMonthCostProfit",keyGenerator = "myKeyGenerator")
    public List<SeriesVO<String>> getMonthCostProfit(BiFilterDTO dto) {
        // 成本 利润
        // 统计成本数据
        List<DateCostVO> dateCostVOS = this.sumCostByDate(dto, 0);
        if (CollUtil.isEmpty(dateCostVOS)){
            return getDateAnalyze(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 12);
        }
        // 销售毛利率
        Map<LocalDate, Map<String, BigDecimal>> costMap = dateCostVOS.stream()
                .collect(Collectors.groupingBy(DateCostVO::getGroupDate,
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));
        Map<Integer, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e.getMonthValue(), v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));

        // 成本
        List<DateCostVO> costSaleExpenses = dateCostVOS.stream()
                .filter(x -> DataSourceCostEnum.COST_SALEEXPENSES.getCode().equals(x.getCostType()))
                .sorted(Comparator.comparing(DateCostVO::getGroupDate))
                .collect(Collectors.toList());
        // 月度成本汇总
        Map<Integer, BigDecimal> monthCostMap = costSaleExpenses.stream()
                .collect(Collectors.toMap(x -> x.getGroupDate().getMonthValue(), DateCostVO::getCostValue));
        // 时间分组销售额-月
        Map<Integer, BigDecimal> monthSalesMap = biOrderInfoService.statisticsSalesByDate(dto, 0);

        // 净利润
        HashMap<Integer, BigDecimal> netProfitMap = new HashMap<>();
        // 表头 SeriesVO
        List<SeriesVO<String>> seriesList = getDateAnalyze(profitMap, monthCostMap, monthSalesMap, netProfitMap, 12);
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
    private List<SeriesVO<String>> getDateAnalyze(Map<Integer, BigDecimal> profitMap, Map<Integer, BigDecimal> costMap, Map<Integer, BigDecimal> salesMap,
                                          HashMap<Integer, BigDecimal> netProfitMap, Integer dateType) {
        List<SeriesVO<String>> seriesList = new ArrayList<>();
        SeriesVO<String> xAxis = new SeriesVO<>();
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
            Optional<Integer> minValue = year1.stream().min(Integer::compareTo);
            if (minValue.isPresent()) {
                startRange = minValue.get();
            }
            endRange = year1.stream().max(Integer::compareTo).get();
        }
        List<String> deptNames = IntStream.rangeClosed(startRange, endRange).mapToObj(x -> StrUtil.format(format, x)).collect(Collectors.toList());

        xAxis.setData(deptNames);
        seriesList.add(xAxis);

        // 必须包含12 个月
        SeriesVO<String> salesVo = new SeriesVO<>();
        salesVo.setName("销售额");
        List<String> salesList = IntStream.rangeClosed(startRange, endRange).mapToObj(x ->
                        salesMap.getOrDefault(x, BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString())
                    .collect(Collectors.toList());
        salesVo.setData(salesList);
        seriesList.add(salesVo);

        SeriesVO<String> costVo = new SeriesVO<>();
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

        SeriesVO<String> netProfitVo = new SeriesVO<>();
        netProfitVo.setName("净利率");
        List<String> netProfitList =IntStream.rangeClosed(startRange, endRange).mapToObj(x ->
                        netProfitMap.getOrDefault(x,BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString())
                    .collect(Collectors.toList());

        netProfitVo.setData(netProfitList);
        seriesList.add(netProfitVo);

        return seriesList;

    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getQuarterCostProfit",keyGenerator = "myKeyGenerator")
    public List<SeriesVO<String>> getQuarterCostProfit(BiFilterDTO dto) {
        // 成本 利润
        // 统计成本数据
        List<DateCostVO> dateCostVOS = this.sumCostByDate(dto, 0);
        if (CollUtil.isEmpty(dateCostVOS)){
            return getDateAnalyze(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 4);
        }
        // 销售毛利率 按照季度汇总数据
        Map<LocalDate, Map<String, BigDecimal>> costMap = dateCostVOS.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate(),
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));
        // 月度数据
        Map<Integer, BigDecimal> profitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e.getMonthValue(), v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));
        // 季度数据  (x- 1) / 3 + 1
        Map<Integer, BigDecimal> quarterMap = profitMap.entrySet().stream()
                .collect(Collectors.groupingBy(x -> (x.getKey() - 1) / 3 + 1,
                        MathUtil.summingBigDecimal(Map.Entry::getValue)));

        // 成本
        List<DateCostVO> costSaleExpenses = dateCostVOS.stream()
                .filter(x -> DataSourceCostEnum.COST_SALEEXPENSES.getCode().equals(x.getCostType()))
                .sorted(Comparator.comparing(DateCostVO::getGroupDate))
                .collect(Collectors.toList());
        // 季度成本汇总
        Map<Integer, BigDecimal> quarterCostMap = costSaleExpenses.stream()
                .collect(Collectors.groupingBy(x -> (x.getGroupDate().getMonthValue() - 1) / 3 + 1,
                        MathUtil.summingBigDecimal(DateCostVO::getCostValue)));

        // 时间分组销售额-季度
        Map<Integer, BigDecimal> quarterSalesMap = biOrderInfoService.statisticsSalesByDate(dto, 1);

        // 净利润 TODO
        HashMap<Integer, BigDecimal> netProfitMap = new HashMap<>(4);
        // 表头 SeriesVO
        List<SeriesVO<String>> seriesList = getDateAnalyze(quarterMap, quarterCostMap, quarterSalesMap, netProfitMap, 4);
        return seriesList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getYearCostProfit",keyGenerator = "myKeyGenerator")
    public List<SeriesVO<String>> getYearCostProfit(BiFilterDTO dto) {
        // 成本 利润
        // 统计成本数据
        List<DateCostVO> dateCostVOS = this.sumCostByDate(dto, 1);
        if (CollUtil.isEmpty(dateCostVOS)){
            return getDateAnalyze(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 0);
        }
        // 销售毛利率
        Map<LocalDate, Map<String, BigDecimal>> costMap = dateCostVOS.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate(),
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));
        // 年度分组数据销售毛利率
        Map<Integer, BigDecimal> yearMap = costMap.keySet().stream().collect(Collectors.groupingBy(e -> e.getYear(), MathUtil.summingBigDecimal(v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        })));

        // 成本
        List<DateCostVO> costSaleExpenses = dateCostVOS.stream()
                .filter(x -> DataSourceCostEnum.COST_SALEEXPENSES.getCode().equals(x.getCostType()))
                .sorted(Comparator.comparing(DateCostVO::getGroupDate))
                .collect(Collectors.toList());
        // 季度成本汇总
        Map<Integer, BigDecimal> yearCostMap = costSaleExpenses.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate().getYear(),
                        MathUtil.summingBigDecimal(DateCostVO::getCostValue)));

        // 时间分组销售额-季度
        Map<Integer, BigDecimal> yearSalesMap = biOrderInfoService.statisticsSalesByDate(dto, 2);

        // 净利润 TODO
        HashMap<Integer, BigDecimal> netProfitMap = new HashMap<>(4);
        // 表头 SeriesVO
        List<SeriesVO<String>> seriesList = getDateAnalyze(yearMap, yearCostMap, yearSalesMap, netProfitMap, 0);
        return seriesList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getDeptCostProfitRank",keyGenerator = "myKeyGenerator")
    public List<CostProfitAnalyzeRankVO> getDeptCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "dept_name", "dept_name");
        return rankResult;
    }

    private List<CostProfitAnalyzeRankVO> getCostProfitAnalyzeRankVOS(BiFilterDTO dto, String groupName, String saleGroupName) {
        // 统计成本数据
        List<DeptCostVO> deptCostVOS = this.sumCostByCondition(dto,groupName);
        if (CollUtil.isEmpty(deptCostVOS)){
            return new ArrayList<>();
        }
        // 销售毛利润
        Map<String, Map<String, BigDecimal>> costMap = deptCostVOS.stream()
                .collect(Collectors.groupingBy(DeptCostVO::getName,
                        Collectors.toMap(DeptCostVO::getCostType, DeptCostVO::getCostValue)));
        Map<String, BigDecimal> deptProfitMap = costMap.keySet().stream().collect(Collectors.toMap(e -> e, v -> {
            Map<String, BigDecimal> tempMap = costMap.get(v);
            BigDecimal costMainBusinessIncome = tempMap.getOrDefault(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), BigDecimal.ZERO);
            BigDecimal costTotalCost = tempMap.getOrDefault(DataSourceCostEnum.COST_TOTALCOST.getCode(), BigDecimal.ZERO);
            BigDecimal costSaleExpenses = tempMap.getOrDefault(DataSourceCostEnum.COST_SALEEXPENSES.getCode(), BigDecimal.ZERO);
            return costMainBusinessIncome.subtract(costTotalCost).subtract(costSaleExpenses);
        }));

        // 销售成本
        Map<String, BigDecimal> deptCostMap = deptCostVOS.stream()
                .filter(x -> DataSourceCostEnum.COST_SALEEXPENSES.getCode().equals(x.getCostType()))
                .collect(Collectors.toMap(DeptCostVO::getName, DeptCostVO::getCostValue));
        // 主营收入
        Map<String, BigDecimal> deptMainMap = deptCostVOS.stream()
                .filter(x -> DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode().equals(x.getCostType()))
                .collect(Collectors.toMap(DeptCostVO::getName, DeptCostVO::getCostValue));
        // 部门销售额
        dto.setStartTime(LocalDateTime.of(LocalDate.from(deptCostVOS.get(0).getMonth().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN));
        dto.setEndTime(LocalDateTime.of(LocalDate.from(deptCostVOS.get(0).getMonth().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX));
        Map<String, BigDecimal> deptSalesMap = biOrderInfoService.statisticsSalesByCondition(dto, saleGroupName);
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
    @Cacheable(cacheNames = "cache:bi:getPlatformCostProfitRank",keyGenerator = "myKeyGenerator")
    public List<CostProfitAnalyzeRankVO> getPlatformCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "platform_name", "source_platform");
        return rankResult;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getShopCostProfitRank",keyGenerator = "myKeyGenerator")
    public List<CostProfitAnalyzeRankVO> getShopCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "shop_name", "shop_name");
        return rankResult;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getUserCostProfitRank",keyGenerator = "myKeyGenerator")
    public List<CostProfitAnalyzeRankVO> getUserCostProfitRank(BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> rankResult = getCostProfitAnalyzeRankVOS(dto, "charge_name", "charge_name");
        return rankResult;
    }

    @Override
    public List<DateCostVO> sumByDateAndCostType(BiFilterDTO dto, List<String> dictValues) {
        List<DateCostVO> vo = baseMapper.sumByDateAndCostType(dto, dictValues);
        return vo;
    }

    @Override
    public List<CompletionRateRankingDTO.PagingDTO> deptCompletionRateRanking(CompletionRateRankingDTO.SearchDTO dto, String settleRate) {
        return baseMapper.deptCompletionRateRanking(dto, settleRate);
    }

    @Override
    public List<CompletionRateRankingDTO.PagingDTO> userCompletionRateRanking(CompletionRateRankingDTO.SearchDTO dto, String settleRate) {
        return baseMapper.userCompletionRateRanking(dto, settleRate);
    }

    /**
     * 通过日期汇总成本数据
     * @param dto
     * @param type  0 返回今年数据  1 返回所有年份数据
     * @return
     */
    private List<DateCostVO> sumCostByDate(BiFilterDTO dto, Integer type) {
        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), DataSourceCostEnum.COST_TOTALCOST.getCode(), DataSourceCostEnum.COST_SALEEXPENSES.getCode()));
        if (0 == type) {
            LocalDateTime startTime = LocalDateTime.of(LocalDate.from(LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear())), LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(LocalDate.from(LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear())), LocalTime.MAX);
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
        }else {
            dto.setStartTime(LocalDateTime.MIN);
            dto.setEndTime(LocalDateTime.now().plusYears(10));
        }

        List<DateCostVO> vo = baseMapper.sumByDateAndCostType(dto, dictValues);
        return vo;
    }

    private List<SeriesVO> getDeptSeriesVo(Map<Integer, BigDecimal> profitMap, List<DateCostVO> costSaleExpenses, Map<Integer, BigDecimal> monthSalesMap) {
        List<SeriesVO> seriesList = new ArrayList<>();
        SeriesVO costVo = new SeriesVO<>();
        costVo.setName("成本");
        List<BigDecimal> costList = Collections.emptyList();
        if (CollUtil.isEmpty(costSaleExpenses)){
            costList = costSaleExpenses.stream().map(DateCostVO::getCostValue).collect(Collectors.toList());
        }
        costVo.setData(costList);
        seriesList.add(costVo);
        SeriesVO profitVo = new SeriesVO<>();
        profitVo.setName("毛利润");
        List<BigDecimal> profitList = Collections.emptyList();
        if (CollUtil.isEmpty(costSaleExpenses)){
            profitList = costSaleExpenses.stream().map(x -> profitMap.get(x.getGroupDate().getMonthValue())).collect(Collectors.toList());
        }
        profitVo.setData(profitList);
        seriesList.add(profitVo);
        SeriesVO yAxis = new SeriesVO<>();
        yAxis.setName("名称");
        List<String> deptNames = Collections.emptyList();
        if (CollUtil.isEmpty(costSaleExpenses)){
            deptNames = costSaleExpenses.stream()
                    .map(x -> x.getGroupDate().getMonthValue() + "月")
                    .collect(Collectors.toList());
        }
        yAxis.setData(deptNames);
        seriesList.add(yAxis);
        SeriesVO salesVo = new SeriesVO<>();
        salesVo.setName("销售额");
        List<BigDecimal> salesList = Collections.emptyList();
        if (CollUtil.isEmpty(salesList)){
            salesList = costSaleExpenses.stream().map(x -> monthSalesMap.get(x.getGroupDate().getMonthValue())).collect(Collectors.toList());
        }
        salesVo.setData(salesList);
        seriesList.add(salesVo);
        return seriesList;

    }

    private static List<SeriesVO<String>> getSeriesVOS(Map<String, BigDecimal> profitMap) {
        List<SeriesVO<String>> seriesList = new ArrayList<>();
        SeriesVO profitVo = new SeriesVO<>();
        profitVo.setName("销售利润");
        List<BigDecimal> profitList = Collections.emptyList();
        if(CollUtil.isNotEmpty(profitMap)){
            profitList = profitMap.keySet().stream().map(x -> profitMap.get(x))
                    .collect(Collectors.toList());
        }
        profitVo.setData(profitList);
        seriesList.add(profitVo);
        SeriesVO yAxis = new SeriesVO<>();
        yAxis.setName("名称");
        List<String> titleList = Collections.emptyList();
        if(CollUtil.isNotEmpty(profitMap)){
            titleList = profitMap.keySet().stream().collect(Collectors.toList());
        }
        yAxis.setData(titleList);
        seriesList.add(yAxis);
        return seriesList;
    }


}
