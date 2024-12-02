package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.erp.model.tms.entity.*;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.server.tms.mapper.LogisticsLargeMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsLargeDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 物流大表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
 */
@Slf4j
@Service
public class LogisticsLargeServiceImpl extends SuperServiceImpl<LogisticsLargeMapper, LogisticsLargeEntity> implements LogisticsLargeService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsLargeDTO.AddDTO addDTO) {
        LogisticsLargeEntity logisticsLargeEntity = new LogisticsLargeEntity();
        BeanMapperUtils.copy(addDTO, logisticsLargeEntity);

        // 数据处理
        handleData(logisticsLargeEntity);

        log.info("开始新增物流大单");
        boolean save = super.save(logisticsLargeEntity);
        if(!save) {
            throw new ServiceException("物流大单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流大单" , logisticsLargeEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, logisticsLargeEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(logisticsLargeEntity.getId(), logisticsLargeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsLargeDTO.UpdateDTO updateDTO) {
        LogisticsLargeEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流大单"));
        LogisticsLargeEntity logisticsLargeEntity =  BeanMapperUtils.map(LogisticsLargeEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsLargeEntity);
        log.info("编辑 开始修改物流大单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsLargeEntity);
        if(!save) {
            throw new ServiceException("物流大单保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录物流大单日志数据，id：【{}】", logisticsLargeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsLargeEntity.getId(), "物流大单");
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsLargeEntity logisticsLargeEntity) {
    // TODO 验证数据 & 数据赋值
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateFirstMileLogisticsTable(FirstMileCostAllocationEntity entity, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, List<FirstMileSkuCostAllocationDetailEntity> skuCostDetailEntityList, FirstMileDeliveryEntity deliveryEntity, List<FirstMileDeliveryDetailEntity> deliveryDetailEntities) {

        //查询物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(entity.getLogisticsBillId());
        //自发货费用
        List<LogisticsBillCostEntity> logisticsBillCostEntities = logisticsBillCostService.listByLogisticsBillIdList(Arrays.asList(logisticsBillEntity.getId()));

        LogisticsLargeDTO.AddDTO addDTO = new LogisticsLargeDTO.AddDTO();

//        tmsFirstMileReconciliationDetailService.listByRelationCode()


        //头程对账单明细(一个头程物流单可生成多次对账单)
        List<TmsFirstMileReconciliationDetailEntity> tmsFirstMileReconciliationDetailEntities = tmsFirstMileReconciliationDetailService.listBySourceIds(Arrays.asList(logisticsBillEntity.getId()), "");
        List<String> fmrIds = tmsFirstMileReconciliationDetailEntities.stream().map(TmsFirstMileReconciliationDetailEntity::getMainId).collect(Collectors.toList());
        List<TmsFirstMileReconciliationEntity> tmsFirstMileReconciliationEntities = tmsFirstMileReconciliationService.listByIds(fmrIds);
        TmsFirstMileReconciliationEntity tmsFirstMileReconciliationEntity = tmsFirstMileReconciliationEntities.stream().filter(req -> req.getReconciliationMonth().equals(entity.getReconciliationMonth())).findFirst().orElse(null);
        addDTO.setOutstockCode(deliveryEntity.getCode());
        addDTO.setOutstockTime(deliveryEntity.getApproveTime());
        addDTO.setPayStatus(tmsFirstMileReconciliationEntity.getPayStatus());
        addDTO.setSkuId(firstMileSkuCostAllocationEntity.getSkuId());
        addDTO.setSkuNo(firstMileSkuCostAllocationEntity.getSkuNo());
        addDTO.setDeliveryQty(firstMileSkuCostAllocationEntity.getDeliveryQty());


//        tmsFirstMileReconciliationService.set
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.UPDATE_STATUS);

    }

    @Override
    public void listLargeDataById(List<String> ids) {
        baseMapper.listLargeDataById(ids);
    }



}
