package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TmsLogisticsBillCostDetailDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.TmsLogisticsBillCostDetailEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.mapper.TmsLogisticsBillCostDetailMapper;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsLogisticsBillCostDetailService;
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
public class TmsLogisticsBillCostDetailServiceImpl extends SuperServiceImpl<TmsLogisticsBillCostDetailMapper, TmsLogisticsBillCostDetailEntity> implements TmsLogisticsBillCostDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;


    @Autowired
    private LogisticsBillCostService logisticsBillCostService;

    @Override
    public Boolean batchAdd(List<TmsLogisticsBillCostDetailDTO.AddDTO> costDetailList, String mainId) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsLogisticsBillCostDetailEntity> list = BeanMapperUtils.copyList(TmsLogisticsBillCostDetailEntity.class, costDetailList);
        // 数据处理
        handleData(list,mainId);
        log.info("开始新增自发货费用明细");

        boolean saveBatch = super.saveBatch(list);
        if(!saveBatch) {
            throw new ServiceException("自发货费用明细保存失败");
        }
        return saveBatch;
    }

    @Override
    public Boolean batchUpdate(List<TmsLogisticsBillCostDetailDTO.UpdateDTO> costDetailList, String mainId) {
        if (CollectionUtils.isEmpty(costDetailList)) {
            return Boolean.TRUE;
        }
        List<TmsLogisticsBillCostDetailEntity> list = BeanMapperUtils.copyList(TmsLogisticsBillCostDetailEntity.class, costDetailList);

        List<TmsLogisticsBillCostDetailEntity> oldList = this.listByMainIdList(Arrays.asList(mainId));

        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TmsLogisticsBillCostDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getCfgCostId())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个费用【%s】", ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }
        // 数据处理
        handleData(list,mainId);
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
    private List<String> getDeleteIds(List<TmsLogisticsBillCostDetailEntity> newList, List<TmsLogisticsBillCostDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TmsLogisticsBillCostDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TmsLogisticsBillCostDetailEntity
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
    public List<TmsLogisticsBillCostDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsLogisticsBillCostDetailEntity::getMainId, mainIdList)
                .list();
    }

    @Override
    public List<TmsLogisticsBillCostDetailDTO.CostCompareDTO> getCostCompareListById(String id) {
        if(StringUtils.isBlank(id)){
            return new ArrayList<>();
        }
        List<TmsLogisticsBillCostDetailDTO.CostCompareDTO> costCompareDTOList = baseMapper.getCostCompareListById(id);
        costCompareDTOList.forEach(v->{
            if(Objects.nonNull(v.getActualCost()) && Objects.nonNull(v.getEstimatedFee())){
                v.setCostDiff(v.getEstimatedFee().subtract(v.getActualCost()));
            }
        });
        return costCompareDTOList;
    }

    @Override
    public List<TmsLogisticsBillCostDetailDTO.CostViewDTO> listCostByMainIdList(List<String> mainIdList) {
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
        lambdaUpdate().in(TmsLogisticsBillCostDetailEntity::getMainId,mainIdList).remove();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<TmsLogisticsBillCostDetailEntity> list,String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        //查询主表数据
        LogisticsBillCostEntity mainEntity = logisticsBillCostService.getById(mainId);
        if (ObjectUtil.isEmpty(mainEntity)) {
            throw new ServiceException("自发货费用不存在");
        }
        //查询汇率
        BigDecimal rate;
        if(StrUtil.equals(CurrencyEnum.CNY.getCurrencyCode(),mainEntity.getCurrency())){
            rate = BigDecimal.ONE;
        }else{
            rate = dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), mainEntity.getCurrency());
            if(ObjectUtil.isEmpty(rate)){
                throw new ServiceException("汇率为空，请维护汇率后再提交");
            }
        }
        for (TmsLogisticsBillCostDetailEntity entity : list) {

            entity.setMainId(mainId);
            //汇率
            entity.setExchangeRate(rate);
            //币别
            entity.setCurrency(mainEntity.getCurrency());
        }
    }
}
