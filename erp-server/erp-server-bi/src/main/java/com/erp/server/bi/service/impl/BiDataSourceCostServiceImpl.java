package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
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
import com.erp.model.bi.vo.TargetSaleSumVO;
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
        List<String> costIds = getCostIds(dto);
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
        List<String> costIds = getCostIds(dto);
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
        List<String> costIds = getCostIds(dto);
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
}
