package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.mapper.TmsCostDetailMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 自发货费用明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-20
 */
@Slf4j
@Service
public class TmsCostDetailServiceImpl extends SuperServiceImpl<TmsCostDetailMapper, TmsCostDetailEntity> implements TmsCostDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @Resource
    private TmsCfgCostService tmsCfgCostService;

    @Override
    public Boolean batchAdd(List<TmsCostDetailDTO.AddDTO> costDetailList, String mainId, DictCostAttributionEnum dictCostAttributionEnum) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsCostDetailEntity> list = BeanMapperUtils.copyList(TmsCostDetailEntity.class, costDetailList);

        // 数据处理
        handleData(list,mainId,dictCostAttributionEnum);

        log.info("开始新增自发货费用明细");

        boolean saveBatch = super.saveOrUpdateBatch(list);
        if(!saveBatch) {
            throw new ServiceException("自发货费用明细保存失败");
        }
        this.validateDbCategoryCurrency(mainId);
        //操作���志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCostName())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), pairList, "编辑操作");
        return saveBatch;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchUpdate(List<TmsCostDetailDTO.UpdateDTO> costDetailList, String mainId,DictCostAttributionEnum dictCostAttributionEnum,Boolean isImport) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsCostDetailEntity> list = BeanMapperUtils.copyList(TmsCostDetailEntity.class, costDetailList);

        //自发货要根据id判断删除
        if (DictCostAttributionEnum.SELF_DELIVER.equals(dictCostAttributionEnum) && !isImport) {
            List<TmsCostDetailEntity> oldList = this.listByMainIdList(Arrays.asList(mainId));

            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                List<TmsCostDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
                //操作日志
                List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCfgCostId())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), pairList, "编辑操作");
                this.removeByIds(deleteIds);
            }
        }
        //头程要根据id判断删除
        if (DictCostAttributionEnum.FIRST_MILE.equals(dictCostAttributionEnum)) {
            List<TmsCostDetailEntity> oldList = this.listByMainIdList(Arrays.asList(mainId));
            oldList = oldList.stream().filter(e -> DetailReconciliationTypeEnum.ACTUAL.getCode().equals(e.getType())).collect(Collectors.toList());
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                List<TmsCostDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
                //操作日志
                List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCfgCostId())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), pairList, "编辑操作");
                this.removeByIds(deleteIds);
            }
        }

        // 数据处理
        handleData(list,mainId,dictCostAttributionEnum);

        log.info("开始更新自发货费用明细");

        if(CollectionUtils.isEmpty(list)){
            return true;
        }

        //添加操作日志
        List<TmsCostDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //添加费用日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCostName())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), addPairList, "编辑操作");
        }

        boolean saveBatch = super.saveOrUpdateBatch(list);
        if(!saveBatch) {
            throw new ServiceException("自发货费用明细保存失败");
        }
        this.validateDbCategoryCurrency(mainId);
        return saveBatch;
    }

    private void validateDbCategoryCurrency(String mainId) {
    	List<TmsCostDetailEntity> list = lambdaQuery().eq(TmsCostDetailEntity::getMainId, mainId).list();
    	if(CollUtil.isEmpty(list)) {
    		return;
    	}
    	Set<String> categoryList = this.validateCategoryCurrency(list);
		if(!categoryList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for(String category : categoryList) {
				sb.append("【");
	    		String[] split = category.split("_");
				sb.append(AllocationFeeTypeEnum.getName(split[0]));
				sb.append("-");
				sb.append(LogisticsBillCostTypeEnum.getName(split[1]));
				sb.append("】");
				sb.append("、");
			}
			throw new ServiceException(sb.substring(0, sb.length() - 1) + "分类下所有一级费用币种必须一致");
		}
    }

    @Override
    public Set<String> validateCategoryCurrency(List<TmsCostDetailEntity> list){
    	Set<String> set = new HashSet<>();
    	if(CollUtil.isEmpty(list)) {
    		return set;
    	}
    	List<String> cfgCostIds = list.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
		Map<String, String> costCategoryMap = tmsCfgCostService.listByIds(cfgCostIds).stream().collect(Collectors.toMap(TmsCfgCostEntity::getId, TmsCfgCostEntity::getDictCostCategory));
		Map<String, List<TmsCostDetailEntity>> costCategoryAddDataDTOMaps = list.stream().filter(l -> l.getCostValue().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.groupingBy(v -> costCategoryMap.get(v.getCfgCostId()) + "_" + v.getType()));
		for(Map.Entry<String, List<TmsCostDetailEntity>> costCategoryAddDataDTOMap : costCategoryAddDataDTOMaps.entrySet()) {
			List<TmsCostDetailEntity> costCategoryList = costCategoryAddDataDTOMap.getValue();
			String categoryCurrency = costCategoryList.get(0).getCurrency();
			if(costCategoryList.stream().anyMatch(d -> !categoryCurrency.equals(d.getCurrency()))) {
				set.add(costCategoryMap.get(costCategoryList.get(0).getCfgCostId()) + "_" + costCategoryList.get(0).getType());
			}
		}
		return set;
    }

    @Override
    public void deleteByMainIdAndCfgCostId(String mainId, String cfgId) {
        if (CharSequenceUtil.isAllBlank(mainId,cfgId)){
            return;
        }
        this.lambdaUpdate().eq(CharSequenceUtil.isNotBlank(mainId),TmsCostDetailEntity::getMainId,mainId)
                .eq(CharSequenceUtil.isNotBlank(cfgId),TmsCostDetailEntity::getCfgCostId,cfgId).remove();
    }

    /**
     * @description: 查询需要删除的id
     * @author Will
     * @date: 2024/3/22 14:51
     * @param newList
     * @param oldList
     * @return List<String>
     */
    private List<String> getDeleteIds(List<TmsCostDetailEntity> newList, List<TmsCostDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TmsCostDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TmsCostDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2024/3/22 14:50
     * @param mainIdList
     * @return List<TmsLogisticsBillCostDetailEntity>
     */
    @Override
    public List<TmsCostDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsCostDetailEntity::getMainId, mainIdList)
                .list();
    }

    @Override
    public List<TmsCostDetailDTO.CostCompareDTO> getCostCompareListByIds(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<TmsCostDetailDTO.CostCompareDTO> costCompareDTOList = baseMapper.getCostCompareListByIds(ids);
//        costCompareDTOList.forEach(v->{
//            if(Objects.nonNull(v.getActualFee()) && Objects.nonNull(v.getEstimatedFee())){
//                v.setFeeDifference(v.getActualFee().subtract(v.getEstimatedFee()));
//            }
//        });
        return costCompareDTOList;
    }

    @Override
    public List<TmsCostDetailDTO.CostViewDTO> listCostByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listCostByMainIdList(mainIdList);
    }

    @Override
    public void deleteByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        lambdaUpdate().in(TmsCostDetailEntity::getMainId,mainIdList).remove();
    }

    @Override
    public List<TmsCostDetailEntity> sumCostByMainIdAndCostId(String logisticsBillCostType, Collection<String> logisticsBillIds, String sourceType) {
        if (CollectionUtils.isEmpty(logisticsBillIds)){
            return Collections.emptyList();
        }
        return this.query()
                .select("SUM(COALESCE(cost_value,0)) as cost_value", "max(currency) as currency" , TmsCostDetailEntity.MAIN_ID, TmsCostDetailEntity.CFG_COST_ID)
                .eq(TmsCostDetailEntity.FIELD_TYPE, logisticsBillCostType)
                .in(TmsCostDetailEntity.MAIN_ID, logisticsBillIds)
                .in(StringUtils.isNotBlank(sourceType), TmsCostDetailEntity.SOURCE_TYPE, sourceType)
                .groupBy(TmsCostDetailEntity.MAIN_ID, TmsCostDetailEntity.CFG_COST_ID)
                .list();
    }

    @Override
    public List<TmsCostDetailEntity> listByCfgCostIdList(List<String> cfgCostIdList) {
        if(CollectionUtils.isEmpty(cfgCostIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsCostDetailEntity::getCfgCostId,cfgCostIdList).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<TmsCostDetailEntity> list, String mainId,DictCostAttributionEnum dictCostAttributionEnum) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        String currency;
        switch (dictCostAttributionEnum) {
            case FIRST_MILE:
                String sourceType = list.stream().map(TmsCostDetailEntity::getSourceType).distinct().findFirst().orElse(null);
                if (SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode().equalsIgnoreCase(sourceType)){
                    // 头程对账单
                    currency = tmsFirstMileReconciliationDetailService.getCurrencyById(mainId);
                } else if (SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode().equalsIgnoreCase(sourceType)){
                    LogisticsBillCostEntity costEntity = logisticsBillCostService.getById(mainId);
                    currency = null == costEntity ? "" : costEntity.getCurrency();
                } else {
                    currency = "CNY";
                }
               break;
            case SELF_DELIVER:
                LogisticsBillCostEntity mainEntity = logisticsBillCostService.getById(mainId);
                currency = ObjectUtil.isEmpty(mainEntity) ? "" : mainEntity.getCurrency();
                break;
            case DECLARE:
                 currency = tmsB2cDeclareReconciliationDetailService.getCurrencyById(mainId);
                break;
            default:
                throw new ServiceException("费用来源类型错误");
        }
        if (ObjectUtil.isEmpty(currency)) {
            log.error("未找到【{}】数据币别,mainId = {}",dictCostAttributionEnum.getName(),mainId);
            throw new ServiceException(CharSequenceUtil.format("未找到【{}】数据币别",dictCostAttributionEnum.getName()));
        }

        List<String> cfgCostIdList = list.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
        List<TmsCostDetailEntity> oldDetailList = listByCfgCostIdListAndMainId(cfgCostIdList, mainId);

        //配置选项
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByIds(cfgCostIdList);
        Map<String, String> costMap = CollUtil.isEmpty(tmsCfgCostList) ? new HashMap<>() : tmsCfgCostList.stream().collect(Collectors.toMap(TmsCfgCostEntity::getId, TmsCfgCostEntity::getCostName));

        Map<String, BigDecimal> rateMap = new HashMap<>();
        rateMap.put("CNY", BigDecimal.ONE);
        for (TmsCostDetailEntity entity : list) {
            entity.setMainId(mainId);
            String costCurrency = entity.getCurrency();
            if(StringUtils.isBlank(costCurrency)) {
            	costCurrency = currency;
            }
            //币别
            entity.setCurrency(costCurrency);
            BigDecimal exchangeRate = rateMap.get(costCurrency);
            if(exchangeRate == null) {
            	//查询汇率
                exchangeRate = dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), costCurrency);
                if(ObjectUtil.isEmpty(exchangeRate)){
                    log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                    throw new ServiceException("汇率为空，请维护汇率后再提交");
                }
                rateMap.put(costCurrency, exchangeRate);
            }
            //汇率
            entity.setExchangeRate(exchangeRate);
            //更新数据无类型默认实际
            entity.setType(CharSequenceUtil.isBlank(entity.getType()) ? LogisticsBillCostTypeEnum.ACTUAL.getCode() : entity.getType());
            //费用名称
            entity.setCostName(costMap.get(entity.getCfgCostId()));
            //主表id
            TmsCostDetailEntity oldDetailEntity = oldDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCfgCostId(), entity.getCfgCostId())
                    && CharSequenceUtil.equals(entity.getType(), obj.getType())
            ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(oldDetailEntity)) {
                entity.setId(oldDetailEntity.getId());
            }
            //操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("编辑了一个费用【{}】,费用值【{}】，币别【{}】",entity.getCostName(),entity.getCostValue(),entity.getCurrency()), ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(),entity.getId(),"编辑操作");
            }
        }
    }

    /**
     * @description: 根据配置 id和主表id查询
     * @author Will
     * @date: 2024/4/15 18:53
     * @param cfgCostIdList
     * @param mainId
     * @return List<TmsCostDetailEntity>
     */
    private List<TmsCostDetailEntity> listByCfgCostIdListAndMainId(List<String> cfgCostIdList,String mainId) {
        if (CollectionUtils.isEmpty(cfgCostIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<TmsCostDetailEntity> list = lambdaQuery().in(TmsCostDetailEntity::getCfgCostId, cfgCostIdList)
                .eq(TmsCostDetailEntity::getMainId, mainId)
                .list();
        return list;
    }

    @Override
    public boolean updateActual0ByMainId(List<String> delActualCostIds) {
        return this.lambdaUpdate()
                .set(TmsCostDetailEntity::getCostValue, BigDecimal.ZERO)
                .eq(TmsCostDetailEntity::getType, LogisticsBillCostTypeEnum.ACTUAL.getCode())
                .in(TmsCostDetailEntity::getMainId, delActualCostIds)
                .update();
    }

    @Override
    public void removeByMainIds(List<String> costIds) {
        if (CollectionUtils.isEmpty(costIds)){
            return;
        }
        this.lambdaUpdate().in(TmsCostDetailEntity::getMainId, costIds).remove();
    }

    @Override
    public Boolean batchImportAdd(List<TmsCostDetailDTO.AddDTO> costDetailList, DictCostAttributionEnum dictCostAttributionEnum) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsCostDetailEntity> list = BeanMapperUtils.copyList(TmsCostDetailEntity.class, costDetailList);
        if (list.stream().anyMatch(e -> StringUtils.isBlank(e.getMainId()))) {
            throw new ServiceException("主表id不能为空");
        }
        Map<String, List<TmsCostDetailEntity>> mainIdMap = list.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        Set<String> mainIds = mainIdMap.keySet();

        Map<String, String> currencyMap = buildCurrencyMap(mainIdMap, dictCostAttributionEnum);
        List<String> cfgCostIdList = list.stream().map(TmsCostDetailEntity::getCfgCostId).distinct().collect(Collectors.toList());
        Map<String, String> costNameMap = buildCostNameMap(cfgCostIdList);
        Map<String, Map<String, TmsCostDetailEntity>> oldDetailMap = buildOldDetailMap(mainIds, cfgCostIdList);

        for (Map.Entry<String, List<TmsCostDetailEntity>> entry : mainIdMap.entrySet()) {
            String mainId = entry.getKey();
            String currency = currencyMap.get(mainId);
            if (ObjectUtil.isEmpty(currency)) {
                log.error("未找到【{}】数据币别,mainId = {}", dictCostAttributionEnum.getName(), mainId);
                throw new ServiceException(CharSequenceUtil.format("未找到【{}】数据币别", dictCostAttributionEnum.getName()));
            }
            handleDataBatch(entry.getValue(), mainId, currency, costNameMap, oldDetailMap.getOrDefault(mainId, new HashMap<>()));
        }

        log.info("开始批量新增自发货费用明细");

        boolean saveBatch = super.saveOrUpdateBatch(list);
        if (!saveBatch) {
            throw new ServiceException("自发货费用明细保存失败");
        }
        validateDbCategoryCurrencyBatch(mainIds);
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCostName())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), pairList, "编辑操作");
        return saveBatch;
    }

    /**
     * 格式化币别
     */
    private Map<String, String> buildCurrencyMap(Map<String, List<TmsCostDetailEntity>> mainIdMap, DictCostAttributionEnum dictCostAttributionEnum) {
        Map<String, String> currencyMap = new HashMap<>();
        Set<String> mainIds = mainIdMap.keySet();
        switch (dictCostAttributionEnum) {
            case SELF_DELIVER:
                logisticsBillCostService.listByIds(mainIds).forEach(e -> currencyMap.put(e.getId(), e.getCurrency()));
                break;
            default:
                throw new ServiceException("费用来源类型错误");
        }
        return currencyMap;
    }

    /**
     * 批量查询费用名称
     */
    private Map<String, String> buildCostNameMap(List<String> cfgCostIdList) {
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByIds(cfgCostIdList);
        return CollUtil.isEmpty(tmsCfgCostList) ? new HashMap<>() : tmsCfgCostList.stream()
                .collect(Collectors.toMap(TmsCfgCostEntity::getId, TmsCfgCostEntity::getCostName));
    }

    /**
     * 批量查询旧数据并转换为map，key为cfgCostId_type，value为费用明细实体
     */
    private Map<String, Map<String, TmsCostDetailEntity>> buildOldDetailMap(Set<String> mainIds, List<String> cfgCostIdList) {
        if (CollectionUtils.isEmpty(cfgCostIdList) || CollectionUtils.isEmpty(mainIds)) {
            return new HashMap<>();
        }
        List<TmsCostDetailEntity> oldDetailList = lambdaQuery()
                .in(TmsCostDetailEntity::getCfgCostId, cfgCostIdList)
                .in(TmsCostDetailEntity::getMainId, mainIds)
                .list();
        Map<String, Map<String, TmsCostDetailEntity>> oldDetailMap = new HashMap<>();
        for (TmsCostDetailEntity entity : oldDetailList) {
            oldDetailMap.computeIfAbsent(entity.getMainId(), k -> new HashMap<>())
                    .put(entity.getCfgCostId() + "_" + entity.getType(), entity);
        }
        return oldDetailMap;
    }

    private void handleDataBatch(List<TmsCostDetailEntity> list, String mainId, String currency,
                                 Map<String, String> costNameMap, Map<String, TmsCostDetailEntity> oldDetailMap) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Set<String> currencySet = list.stream()
                .map(e -> StringUtils.isBlank(e.getCurrency()) ? currency : e.getCurrency())
                .collect(Collectors.toSet());
        Map<String, BigDecimal> rateMap = batchGetRates(currencySet);
        List<Pair<String, String>> logPairs = new ArrayList<>();
        for (TmsCostDetailEntity entity : list) {
            entity.setMainId(mainId);
            String costCurrency = entity.getCurrency();
            if (StringUtils.isBlank(costCurrency)) {
                costCurrency = currency;
            }
            entity.setCurrency(costCurrency);
            BigDecimal exchangeRate = rateMap.get(costCurrency);
            if (exchangeRate == null) {
                throw new ServiceException("汇率为空，请维护汇率后再提交");
            }
            entity.setExchangeRate(exchangeRate);
            entity.setType(CharSequenceUtil.isBlank(entity.getType()) ? LogisticsBillCostTypeEnum.ACTUAL.getCode() : entity.getType());
            entity.setCostName(costNameMap.get(entity.getCfgCostId()));
            TmsCostDetailEntity oldDetailEntity = oldDetailMap.get(entity.getCfgCostId() + "_" + entity.getType());
            if (ObjectUtil.isNotEmpty(oldDetailEntity)) {
                entity.setId(oldDetailEntity.getId());
            }
            if (StringUtils.isNotBlank(entity.getId())) {
                String value = entity.getCostName() + "】,费用值【" + entity.getCostValue() + "】，币别【" + entity.getCurrency();
                logPairs.add(new Pair<>(entity.getId(), value));
            }
        }
        if (CollectionUtils.isNotEmpty(logPairs)) {
            operateLogService.batchAddModuleOperateLog("编辑了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), logPairs, "编辑操作");
        }
    }

    /**
     * 批量查询汇率
     */
    private Map<String, BigDecimal> batchGetRates(Set<String> currencySet) {
        Map<String, BigDecimal> rateMap = new HashMap<>();
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        rateMap.put("CNY", BigDecimal.ONE);
        for (String curr : currencySet) {
            if ("CNY".equals(curr)) {
                continue;
            }
            BigDecimal rate = dmpTaskFeign.getRate(today, curr);
            if (ObjectUtil.isEmpty(rate)) {
                throw new ServiceException("币别【" + curr + "】汇率为空，请维护汇率后再提交");
            }
            rateMap.put(curr, rate);
        }
        return rateMap;
    }

    /**
     * @param mainIds
     * @return void
     * @description: 批量校验数据库数据分类币别
     * @author Will
     * @date: 2026/04/02 19:52
     */
    private void validateDbCategoryCurrencyBatch(Set<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return;
        }
        List<TmsCostDetailEntity> list = lambdaQuery().in(TmsCostDetailEntity::getMainId, mainIds).list();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> cfgCostIds = list.stream().map(TmsCostDetailEntity::getCfgCostId).distinct().collect(Collectors.toList());
        Map<String, String> costCategoryMap = tmsCfgCostService.listByIds(cfgCostIds).stream()
                .collect(Collectors.toMap(TmsCfgCostEntity::getId, TmsCfgCostEntity::getDictCostCategory));
        Map<String, List<TmsCostDetailEntity>> listByMainId = list.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        for (Map.Entry<String, List<TmsCostDetailEntity>> entry : listByMainId.entrySet()) {
            Set<String> categoryList = validateCategoryCurrency(entry.getValue(), costCategoryMap);
            if (!categoryList.isEmpty()) {
                throw new ServiceException(buildCategoryErrorMsg(categoryList));
            }
        }
    }

    /**
     * @param list
     * @param costCategoryMap
     * @return Set<String>
     * @description: 校验分类币别
     * @author Will
     * @date: 2026/04/02 19:52
     */
    private Set<String> validateCategoryCurrency(List<TmsCostDetailEntity> list, Map<String, String> costCategoryMap) {
        Set<String> set = new HashSet<>();
        if (CollUtil.isEmpty(list)) {
            return set;
        }
        Map<String, List<TmsCostDetailEntity>> costCategoryAddDataDTOMaps = list.stream()
                .filter(l -> l.getCostValue().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.groupingBy(v -> costCategoryMap.get(v.getCfgCostId()) + "_" + v.getType()));
        for (Map.Entry<String, List<TmsCostDetailEntity>> costCategoryAddDataDTOMap : costCategoryAddDataDTOMaps.entrySet()) {
            List<TmsCostDetailEntity> costCategoryList = costCategoryAddDataDTOMap.getValue();
            String categoryCurrency = costCategoryList.get(0).getCurrency();
            if (costCategoryList.stream().anyMatch(d -> !categoryCurrency.equals(d.getCurrency()))) {
                set.add(costCategoryMap.get(costCategoryList.get(0).getCfgCostId()) + "_" + costCategoryList.get(0).getType());
            }
        }
        return set;
    }

    /**
     * @param categoryList
     * @return String
     * @description: 构建分类错误信息
     * @author Will
     * @date: 2026/04/02 19:52
     */
    private String buildCategoryErrorMsg(Set<String> categoryList) {
        StringBuilder sb = new StringBuilder();
        for (String category : categoryList) {
            sb.append("【");
            String[] split = category.split("_");
            sb.append(AllocationFeeTypeEnum.getName(split[0]));
            sb.append("-");
            sb.append(LogisticsBillCostTypeEnum.getName(split[1]));
            sb.append("】");
            sb.append("、");
        }
        return sb.substring(0, sb.length() - 1) + "分类下所有一级费用币种必须一致";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchImportUpdate(List<TmsCostDetailDTO.UpdateDTO> costDetailList, DictCostAttributionEnum dictCostAttributionEnum, Boolean isImport) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsCostDetailEntity> list = BeanMapperUtils.copyList(TmsCostDetailEntity.class, costDetailList);
        if (list.stream().anyMatch(e -> StringUtils.isBlank(e.getMainId()))) {
            throw new ServiceException("主表id不能为空");
        }
        Map<String, List<TmsCostDetailEntity>> mainIdMap = list.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        Set<String> mainIds = mainIdMap.keySet();

        //删除逻辑: 自发货
        if (DictCostAttributionEnum.SELF_DELIVER.equals(dictCostAttributionEnum) && !Boolean.TRUE.equals(isImport)) {
            List<TmsCostDetailEntity> oldList = this.listByMainIdList(new ArrayList<>(mainIds));
            Map<String, List<TmsCostDetailEntity>> oldMap = oldList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
            List<String> deleteIds = new ArrayList<>();
            List<Pair<String, String>> deletePairs = new ArrayList<>();
            for (Map.Entry<String, List<TmsCostDetailEntity>> entry : oldMap.entrySet()) {
                List<TmsCostDetailEntity> newList = mainIdMap.getOrDefault(entry.getKey(), Collections.emptyList());
                List<String> delIds = getDeleteIds(newList, entry.getValue());
                if (CollectionUtils.isNotEmpty(delIds)) {
                    deleteIds.addAll(delIds);
                    entry.getValue().stream().filter(obj -> delIds.contains(obj.getId()))
                            .forEach(obj -> deletePairs.add(new Pair<>(obj.getMainId(), obj.getCfgCostId())));
                }
            }
            if (CollectionUtils.isNotEmpty(deletePairs)) {
                operateLogService.batchAddModuleOperateLog("删除了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), deletePairs, "编辑操作");
            }
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);
            }
        }
        //删除逻辑: 头程只处理实际
        if (DictCostAttributionEnum.FIRST_MILE.equals(dictCostAttributionEnum)) {
            List<TmsCostDetailEntity> oldList = this.listByMainIdList(new ArrayList<>(mainIds));
            oldList = oldList.stream().filter(e -> DetailReconciliationTypeEnum.ACTUAL.getCode().equals(e.getType())).collect(Collectors.toList());
            Map<String, List<TmsCostDetailEntity>> oldMap = oldList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
            List<String> deleteIds = new ArrayList<>();
            List<Pair<String, String>> deletePairs = new ArrayList<>();
            for (Map.Entry<String, List<TmsCostDetailEntity>> entry : oldMap.entrySet()) {
                List<TmsCostDetailEntity> newList = mainIdMap.getOrDefault(entry.getKey(), Collections.emptyList());
                List<String> delIds = getDeleteIds(newList, entry.getValue());
                if (CollectionUtils.isNotEmpty(delIds)) {
                    deleteIds.addAll(delIds);
                    entry.getValue().stream().filter(obj -> delIds.contains(obj.getId()))
                            .forEach(obj -> deletePairs.add(new Pair<>(obj.getMainId(), obj.getCfgCostId())));
                }
            }
            if (CollectionUtils.isNotEmpty(deletePairs)) {
                operateLogService.batchAddModuleOperateLog("删除了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), deletePairs, "编辑操作");
            }
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);
            }
        }

        Map<String, String> currencyMap = buildCurrencyMap(mainIdMap, dictCostAttributionEnum);
        List<String> cfgCostIdList = list.stream().map(TmsCostDetailEntity::getCfgCostId).distinct().collect(Collectors.toList());
        Map<String, String> costNameMap = buildCostNameMap(cfgCostIdList);
        Map<String, Map<String, TmsCostDetailEntity>> oldDetailMap = buildOldDetailMap(mainIds, cfgCostIdList);

        for (Map.Entry<String, List<TmsCostDetailEntity>> entry : mainIdMap.entrySet()) {
            String mainId = entry.getKey();
            String currency = currencyMap.get(mainId);
            if (ObjectUtil.isEmpty(currency)) {
                log.error("未找到【{}】数据币别,mainId = {}", dictCostAttributionEnum.getName(), mainId);
                throw new ServiceException(CharSequenceUtil.format("未找到【{}】数据币别", dictCostAttributionEnum.getName()));
            }
            handleDataBatch(entry.getValue(), mainId, currency, costNameMap, oldDetailMap.getOrDefault(mainId, new HashMap<>()));
        }

        //新增费用日志
        List<TmsCostDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCostName())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), addPairList, "编辑操作");
        }

        boolean saveBatch = super.saveOrUpdateBatch(list);
        if (!saveBatch) {
            throw new ServiceException("自发货费用明细保存失败");
        }
        validateDbCategoryCurrencyBatch(mainIds);
        return saveBatch;
    }
}