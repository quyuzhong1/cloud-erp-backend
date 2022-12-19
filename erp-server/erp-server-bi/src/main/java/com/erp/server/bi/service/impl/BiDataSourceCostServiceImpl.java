package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.model.dmp.entity.BiDataSourceCostDetailEntity;
import com.erp.model.dmp.entity.BiDataSourceCostEntity;
import com.erp.server.bi.enums.BiDataSourceCostEnum;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.mapper.BiDataSourceCostMapper;
import com.erp.server.bi.service.BiDataSourceCostDetailService;
import com.erp.server.bi.service.BiDataSourceCostService;
import com.erp.server.bi.service.BiDictService;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCostSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BiDataSourceCostSearchDTO params = dto.getParams();
        IPage<LinkedHashMap<String,Object>> pageData = baseMapper.paging(query, params);
        List<LinkedHashMap<String, Object>> linkedHashMaps = renewBiDataSourceCost(pageData.getRecords());
        pageData.setRecords(linkedHashMaps);
        return new PagingVO(pageData);
    }

    @Override
    public TargetSaleSumVO sumSalesProfit(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost", "cost_saleExpenses"));
        // 获取成本详情ids
        List<String> costIds = getCostIds(dto);
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
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

    private List<String> getCostIds(BiFilterDTO dto) {
        // 查询最新月份数据
        QueryWrapper<BiDataSourceCostEntity> queryWrapper = new QueryWrapper();
        queryWrapper.select("max(month) as month");
        BiDataSourceCostEntity maxMonthEntity = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(maxMonthEntity)) {
            return new ArrayList<>();
        }
        List<BiDataSourceCostEntity> dataSourceCostList = lambdaQuery()
                .in(CollectionUtils.isNotEmpty(dto.getSite()), BiDataSourceCostEntity::getSite, dto.getSite())
                .eq(CollectionUtils.isNotEmpty(dto.getShopName()), BiDataSourceCostEntity::getShopName, dto.getShopName())
                .eq(CollectionUtils.isNotEmpty(dto.getDepartment()), BiDataSourceCostEntity::getDeptName, dto.getDepartment())
                .eq(BiDataSourceCostEntity::getMonth, maxMonthEntity.getMonth())
                .list();
        if (CollectionUtils.isEmpty(dataSourceCostList)) {
            return new ArrayList<>();
        }
        List<String> costIds = dataSourceCostList.stream().map(BiDataSourceCostEntity::getId).distinct().collect(Collectors.toList());
        return costIds;
    }

    @Override
    public TargetSaleSumVO sumSalesRatio(BiFilterDTO dto) {
        // 数据字典获取主营收入  成本合计  销售费用小计 的value
        List<String> dictValues = new ArrayList<>(Arrays.asList("cost_mainBusinessIncome", "cost_totalCost", "cost_saleExpenses"));
        // 获取成本详情ids
        List<String> costIds = getCostIds(dto);
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
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
        List<String> costIds = getCostIds(dto);
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
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
        List<String> costIds = getCostIds(dto);
        if (CollectionUtils.isEmpty(costIds)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 获取详情数据并转为 map 计算
        HashMap<String, Map<String, BigDecimal>> dataSourceCostDetailMap = biDataSourceCostDetailService.convertListByCostIds(costIds, dictValues);
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
        List<LinkedHashMap<String, Object>> costList = renewBiDataSourceCost(list);
        if (CollectionUtils.isEmpty(costList)) {
            return;
        }
        List<String> heads = new ArrayList<>();		//表头信息
        String head = "成本数据表";
        String fileName = dmpOrderInfoService.getFileName("退货数据导出")+ ".xlsx";
        BiDataSourceCostEnum[] values = BiDataSourceCostEnum.values();
        List<String> enumList = Arrays.stream(values).map(BiDataSourceCostEnum::getName).collect(Collectors.toList());
        heads.addAll(enumList);
        //查询成本字典数据
        List<BiDictEntity> dictList = biDictService.listEntityByType(DictEnum.DATASOURCECOST.getType());
        if (CollectionUtils.isNotEmpty(dictList)) {
            List<String> nameList = dictList.stream().map(BiDictEntity::getName).collect(Collectors.toList());
            heads.addAll(nameList);
        }
        ExcelUtil.easyUtil(heads,head,list,fileName);
        return;
    }

    @Override
    public void importExcel(MultipartFile excelFile, HttpServletResponse response) {
        List<Map<String,String>> list = ExcelPrintUtils.makeData(excelFile);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Map<String,String> map:list) {
            //遍历map下的数据
            Iterator<Map.Entry<String, String>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
            //旧数据时记录
            if (ObjectUtils.isNotEmpty(iterator)) {
                while (iterator .hasNext()){
                    Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
                    String key =  entry.getKey().toString();
                    String value = entry.getValue().toString();
                    String name = BiDataSourceCostEnum.getCodeByName(key);
                    if (StringUtils.isBlank(name))  {

                    }
                }
            }
        }


    }


    /**
     * 返回字段处理
     */
    private List<LinkedHashMap<String,Object>> renewBiDataSourceCost(List<LinkedHashMap<String,Object>> list) {
        if (CollectionUtils.isEmpty(list))  {
            return list;
        }
        //查询成本字典数据
        List<BiDictEntity> dictList = biDictService.listEntityByType(DictEnum.DATASOURCECOST.getType());
        if (CollectionUtils.isEmpty(dictList))  {
            return list;
        }
        List<String> costIds = list.stream().map((Map m) -> (String) m.get("id")).collect(Collectors.toList());
        List<BiDataSourceCostDetailEntity> biDataSourceCostDetailList= biDataSourceCostDetailService.listByCostIds(costIds);
        if (CollectionUtils.isEmpty(biDataSourceCostDetailList)) {
            return list;
        }
        for (LinkedHashMap<String,Object> map: list) {
            BiDataSourceCostEnum[] values = BiDataSourceCostEnum.values();
            for (BiDataSourceCostEnum value:values) {
                map.put(value.getName(),map.get(value.getCode()));
            }
            for (BiDictEntity dcit : dictList) {
                BigDecimal value = biDataSourceCostDetailList.stream().filter(obj -> obj.getCostId().equals(map.get("id")) && obj.getCostType().equals(dcit.getValue()))
                        .map(BiDataSourceCostDetailEntity::getCostValue).findFirst().orElse(BigDecimal.ZERO);
                map.put(dcit.getName(),value);
            }
        }
        return list;
    }
}
