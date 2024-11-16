package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;

    @Autowired
    private LogisticsBillCostService logisticsBillCostService;

    @Autowired
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

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
        return saveBatch;
    }


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
        boolean saveBatch = super.saveOrUpdateBatch(list);
        if(!saveBatch) {
            throw new ServiceException("自发货费用明细保存失败");
        }
        return saveBatch;
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
        costCompareDTOList.forEach(v->{
            if(Objects.nonNull(v.getActualFee()) && Objects.nonNull(v.getEstimatedFee())){
                v.setFeeDifference(v.getActualFee().subtract(v.getEstimatedFee()));
            }
        });
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
                .select("SUM(COALESCE(cost_value,0)) as cost_value", TmsCostDetailEntity.MAIN_ID, TmsCostDetailEntity.CFG_COST_ID)
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
                    currency = "";
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
            throw new ServiceException(StrUtil.format("未找到【{}】数据币别",dictCostAttributionEnum.getName()));
        }

        List<String> cfgCostIdList = list.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
        List<TmsCostDetailEntity> oldDetailList = listByCfgCostIdListAndMainId(cfgCostIdList, mainId);

        //查询汇率
        BigDecimal  rate = dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), currency);
        if(ObjectUtil.isEmpty(rate)){
            log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
            throw new ServiceException("汇率为空，请维护汇率后再提交");
        }
        for (TmsCostDetailEntity entity : list) {

            entity.setMainId(mainId);
            //汇率
            entity.setExchangeRate(rate);
            //币别
            entity.setCurrency(currency);
            //更新数据无类型默认实际
            entity.setType(StrUtil.isBlank(entity.getType()) ? LogisticsBillCostTypeEnum.ACTUAL.getCode() : entity.getType());

            //主表id
            TmsCostDetailEntity oldDetailEntity = oldDetailList.stream().filter(obj -> StrUtil.equals(obj.getCfgCostId(), entity.getCfgCostId())
                    && StrUtil.equals(entity.getType(), obj.getType())
            ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(oldDetailEntity)) {
                entity.setId(oldDetailEntity.getId());
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
}
