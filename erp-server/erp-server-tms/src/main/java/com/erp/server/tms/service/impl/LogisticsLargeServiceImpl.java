package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.tms.enums.ReconciliationBillTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private FirstMileEstimatedBillService firstMileEstimatedBillService;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private TmsCostDetailService logisticsBillCostDetailService;

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
        List<LogisticsLargeEntity> list = this.lambdaQuery().eq(LogisticsLargeEntity::getOutstockCode, entity.getSourceCode()).list();
        LogisticsLargeEntity logisticsLargeEntity = list.stream().filter(req -> ReconciliationBillTypeEnum.ACTUAL.getCode().equals(req.getReconciliationBillType())).findFirst().orElse(null);
        if (logisticsLargeEntity != null) {
            throw new ServiceException("实际账单已添加物流大表，请不要重复添加");
        }
        LogisticsLargeEntity largeEntity = list.stream().filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType())).findFirst().orElse(null);
        if (logisticsLargeEntity != null) {
            throw new ServiceException("预估账单已添加物流大表，请不要重复添加");
        }

        //查询物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(entity.getLogisticsBillId());

        //查询物流详情
        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Arrays.asList(logisticsBillEntity.getId()));

        //自发货费用
        List<LogisticsBillCostEntity> logisticsBillCostEntities = logisticsBillCostService.listByLogisticsBillIdList(Arrays.asList(logisticsBillEntity.getId()));


        //物流商信息
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(logisticsBillEntity.getId());

        //查询供应商
        SupplierEntity supplierEntity = supplierFeign.getSupplierById(logisticsSupplierEntity.getSupplierId());

        //头程对账单主信息
        TmsFirstMileReconciliationEntity reconciliationEntity = tmsFirstMileReconciliationService.getById(entity.getReconciliationId());

        //头程对账明细信息
        TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity = tmsFirstMileReconciliationDetailService.getById(firstMileSkuCostAllocationEntity.getReconciliationDetailId());


        //查询仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(deliveryEntity.getDeliveryWarehouseId(), deliveryEntity.getDestWarehouseId()));

        //头程费用分摊费用明细表
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntities = firstMileSkuCostAllocationDetailService.listByMainIds(Arrays.asList(entity.getId()));

        //物流暂估账单
        List<FirstMileEstimatedBillDTO.View> estimatedBillView = firstMileEstimatedBillService.listByLogisticsBillIds(Arrays.asList(entity.getLogisticsBillId()), ConfirmStatusEnum.CONFIRM.getCode());

        //运费
        FirstMileSkuCostAllocationDetailEntity costAllocationDetailEntity = skuCostAllocationDetailEntities.stream().filter(req -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(req.getFeeType())).findFirst().orElse(null);
        List<String> ids = logisticsBillCostEntities.stream().map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
        List<TmsCostDetailDTO.CostCompareDTO> costCompareDTOList = logisticsBillCostDetailService.getCostCompareListByIds(ids);

        LogisticsLargeDTO.AddDTO addDTO = new LogisticsLargeDTO.AddDTO();
        addDTO.setOutstockCode(deliveryEntity.getCode());
        addDTO.setOutstockTime(deliveryEntity.getApproveTime());
        //付款状态

        if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(firstMileSkuCostAllocationEntity.getBillSourceType())) {
            addDTO.setPayStatus(reconciliationEntity.getPayStatus());
            addDTO.setFreightCurrency(reconciliationEntity.getCurrency());
            //实际账单取【头程对账单】的汇率
            BigDecimal exchangeRate = reconciliationEntity.getExchangeRate();
            addDTO.setFirstMileEstimatedFreightTax(costAllocationDetailEntity.getAllocatedAmount().divide(exchangeRate, 4, RoundingMode.DOWN));

        } else {
            addDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());


            BigDecimal exchangeRate = BigDecimal.ONE;
            //预估账单取【物流单】的汇率
            if (CollUtil.isNotEmpty(logisticsBillCostEntities)) {
                String currency = logisticsBillCostEntities.get(0).getCurrency();
                exchangeRate = dmpTaskFeign.getRate(logisticsBillEntity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), currency);
            }

            BigDecimal firstMileEstimatedFreight = BigDecimal.ZERO;
            BigDecimal firstMileActualFreight = BigDecimal.ZERO;
            for (TmsCostDetailDTO.CostCompareDTO costCompareDTO : costCompareDTOList) {
                firstMileEstimatedFreight = firstMileEstimatedFreight.add(costCompareDTO.getEstimatedFee().divide(BigDecimal.ONE.add(exchangeRate)));


                //头程物流单实际运费
                firstMileActualFreight = firstMileActualFreight.add(costCompareDTO.getActualFee().divide(BigDecimal.ONE.add(exchangeRate)));
                addDTO.setFirstMileActualFreight(firstMileActualFreight);
            }
        }

        addDTO.setSkuId(firstMileSkuCostAllocationEntity.getSkuId());
        addDTO.setSkuNo(firstMileSkuCostAllocationEntity.getSkuNo());
        addDTO.setDeliveryQty(firstMileSkuCostAllocationEntity.getDeliveryQty());
        addDTO.setWeight(reconciliationDetailEntity.getBillingWeight());
        addDTO.setLogisticsBillingWeight(reconciliationDetailEntity.getVolumeWeight());
        addDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
        addDTO.setLogisticsSupplierId(logisticsSupplierEntity.getId());
        addDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
        addDTO.setPaymentCompanyName(supplierEntity.getPaymentCompanyName());
        addDTO.setTransportNo(logisticsBillEntity.getCounterNo());
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> deliveryEntity.getDeliveryWarehouseId().equals(req.getId())).findFirst().orElse(null);
        if (deliveryWarehouse != null) {
            addDTO.setOriginPort(deliveryWarehouse.getAddress());
            addDTO.setPickupAddress(deliveryWarehouse.getAddress());
        }
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream().filter(req -> deliveryEntity.getDestWarehouseId().equals(req.getId())).findFirst().orElse(null);
        if (destWarehouse != null) {
            addDTO.setDestinationPort(destWarehouse.getAddress());
            addDTO.setDeliveryAddress(destWarehouse.getAddress());
        }

        LogisticsBillDetailEntity billDetailEntity = detailEntityList.stream().filter(req -> FmLogisticTrackStatusEnum.PICKUP.getCode().equals(req.getTrackStatus())).findFirst().orElse(null);
        if (billDetailEntity != null) {
            addDTO.setPickupTime(billDetailEntity.getTrackTime());
        }
        if (CollUtil.isNotEmpty(detailEntityList)) {
            addDTO.setActualDeliveryTime(detailEntityList.get(0).getSignTime());
        }

        if (costAllocationDetailEntity != null && costAllocationDetailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            //[运费计算系数]头程分摊金额/头程金额
            addDTO.setFreightCalculationFactor(costAllocationDetailEntity.getAllocatedAmount().divide(costAllocationDetailEntity.getAmount(), 6, RoundingMode.DOWN));
        }

        // TODO
        addDTO.setBillTotalAmount(BigDecimal.ZERO);

        addDTO.setFirstMileActualFreightTax(costAllocationDetailEntity.getAllocatedAmount());

        addDTO.setFirstMileEstimatedFreight(firstMileEstimatedFreight);

//        tmsFirstMileReconciliationService.set
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public void listLargeDataById(List<String> ids) {
        baseMapper.listLargeDataById(ids);
    }



    @Override
    public List<LogisticsLargeEntity> listByIdSourceId(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(LogisticsLargeEntity::getSourceId, ids).list();
    }

}
