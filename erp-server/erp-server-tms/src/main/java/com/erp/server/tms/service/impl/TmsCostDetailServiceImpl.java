package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.mapper.TmsCostDetailMapper;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.erp.server.tms.service.TmsCostDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    @Override
    public Boolean batchAdd(List<TmsCostDetailDTO.AddDTO> costDetailList, String mainId, DictCostAttributionEnum dictCostAttributionEnum) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsCostDetailEntity> list = BeanMapperUtils.copyList(TmsCostDetailEntity.class, costDetailList);
        // 数据处理
        handleData(list,mainId,dictCostAttributionEnum);
        log.info("开始新增自发货费用明细");

        boolean saveBatch = super.saveBatch(list);
        if(!saveBatch) {
            throw new ServiceException("自发货费用明细保存失败");
        }
        return saveBatch;
    }

    @Override
    public Boolean batchUpdate(List<TmsCostDetailDTO.UpdateDTO> costDetailList, String mainId,DictCostAttributionEnum dictCostAttributionEnum) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsCostDetailEntity> list = BeanMapperUtils.copyList(TmsCostDetailEntity.class, costDetailList);

        List<TmsCostDetailEntity> oldList = this.listByMainIdList(Arrays.asList(mainId));

        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TmsCostDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCfgCostId())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
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
    public List<TmsCostDetailDTO.CostCompareDTO> getCostCompareListById(String id) {
        if(StringUtils.isBlank(id)){
            return new ArrayList<>();
        }
        List<TmsCostDetailDTO.CostCompareDTO> costCompareDTOList = baseMapper.getCostCompareListById(id);
        costCompareDTOList.forEach(v->{
            if(Objects.nonNull(v.getActualCost()) && Objects.nonNull(v.getEstimatedFee())){
                v.setCostDiff(v.getEstimatedFee().subtract(v.getActualCost()));
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
    public List<TmsCostDetailEntity> sumCostByMainIdAndCostId(String logisticsBillCostType, List<String> logisticsBillIds) {
        return this.query()
                .select("SUM(COALESCE(cost_value,0)) as cost_value", TmsCostDetailEntity.MAIN_ID, TmsCostDetailEntity.CFG_COST_ID)
                .eq(TmsCostDetailEntity.TYPE, LogisticsBillCostTypeEnum.ESTIMATED.getCode())
                .in(TmsCostDetailEntity.MAIN_ID, logisticsBillIds)
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
                currency = CurrencyEnum.CNY.getCurrencyCode();
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
        }
    }
}
