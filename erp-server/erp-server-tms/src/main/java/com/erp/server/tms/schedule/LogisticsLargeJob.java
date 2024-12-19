package com.erp.server.tms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.ReconciliationBillTypeEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationMainBigTableStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationMainReportStatusEnum;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LogisticsLargeJob {

    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;

    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;

    @Resource
    private SmallBagCostAllocationDetailService smallBagCostAllocationDetailService;

    @Resource
    private LogisticsLargeService logisticsLargeService;

    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    @Resource
    private TransferDeclareCostAllocationService transferDeclareCostAllocationService;

    @Resource
    private TransferDeclareCostAllocationDetailService transferDeclareCostAllocationDetailService;

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    @Resource
    private TmsB2cDeclareReconciliationService tmsB2cDeclareReconciliationService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private FirstMileSkuCostAllocationService firstMileSkuCostAllocationService;

    @Resource
    private FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private SoB2cFeign soB2cFeign;


    /**
     * 小包费用分摊自动生成物流大表
     */
    @XxlJob("smallBagAllocationToLogisticsLarge")
    public void smallBagAllocationToLogisticsLarge() {
        List<SmallBagCostAllocationMainEntity> allocationMainEntityList = smallBagCostAllocationMainService.lambdaQuery()
                .eq(SmallBagCostAllocationMainEntity::getBigTableStatus, SmallBagCostAllocationMainBigTableStatusEnum.TODO.getCode())
                .eq(SmallBagCostAllocationMainEntity::getReportStatus, SmallBagCostAllocationMainReportStatusEnum.CONFIRMED.getCode())
                .orderByDesc(SmallBagCostAllocationMainEntity::getReportDate)
                .list();

        List<String> ids = allocationMainEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        List<SmallBagCostAllocationEntity> costAllocationEntityList = smallBagCostAllocationService.lambdaQuery().in(SmallBagCostAllocationEntity::getMainId, ids).list();
        List<String> costAllocationIds = costAllocationEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SmallBagCostAllocationDetailEntity> smallBagCostAllocationDetailEntities = smallBagCostAllocationDetailService.listByMainIds(costAllocationIds);

        //销售出库单
        List<String> outstockDetailIds = costAllocationEntityList.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailList = new ArrayList<>();
        if (CollUtil.isNotEmpty(outstockDetailIds)) {
            soOutstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class).in(SoOutstockDetailEntity::getId, outstockDetailIds).list();
        }
        List<String> outstockIds = soOutstockDetailList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntitylList = new ArrayList<>();
        if (CollUtil.isNotEmpty(outstockIds)) {
            soOutstockEntitylList = FeignQuery.create(SoOutstockEntity.class).in(SoOutstockEntity::getId, outstockIds).list();
        }

        List<String> soIds = soOutstockEntitylList.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(soIds)) {
            soB2cEntities = soB2cFeign.listByIds(soIds);
        }

        for (SmallBagCostAllocationMainEntity entity : allocationMainEntityList) {
            List<SmallBagCostAllocationEntity> costAllocationEntities = costAllocationEntityList.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(costAllocationEntities)) {
                XxlJobHelper.log("小包费用分摊表信息不存在!");
                continue;
            }
            List<String> costAllocationIdList = costAllocationEntities.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
            List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntityList = smallBagCostAllocationDetailEntities.stream().filter(req -> costAllocationIdList.contains(req.getMainId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(costAllocationDetailEntityList)) {
                XxlJobHelper.log("小包费用分摊明细表信息不存在!");
                continue;
            }

            List<String> outDetailId = costAllocationEntities.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
            List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailList.stream().filter(req -> outDetailId.contains(req.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(soOutstockDetailEntities)) {
                XxlJobHelper.log("销售出库详情不存在!");
                continue;
            }
            SoOutstockEntity soOutstockEntity = soOutstockEntitylList.stream().filter(req -> req.getId().equals(soOutstockDetailEntities.get(0).getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soOutstockEntity)) {
                XxlJobHelper.log("销售出库详情不存在!");
                continue;
            }

            logisticsLargeService.generateSmallBagCostAllocationTable(entity, costAllocationEntities, costAllocationDetailEntityList, soOutstockEntity, soOutstockDetailEntities, soB2cEntities);
        }
    }



    /**
     * 中转费用分摊自动生成物流大表
     */
    @XxlJob("transferDeclareCostAllocationToLogisticsLarge")
    public void transferDeclareCostAllocationToLogisticsLarge() {
        List<TransferDeclareCostAllocationMainEntity> allocationMainEntityList = transferDeclareCostAllocationMainService.lambdaQuery()
                .eq(TransferDeclareCostAllocationMainEntity::getBigTableStatus, SmallBagCostAllocationMainBigTableStatusEnum.TODO.getCode())
                .eq(TransferDeclareCostAllocationMainEntity::getReportStatus, SmallBagCostAllocationMainReportStatusEnum.CONFIRMED.getCode())
                .orderByDesc(TransferDeclareCostAllocationMainEntity::getReportDate)
                .list();


        List<String> ids = allocationMainEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<TransferDeclareCostAllocationEntity> costAllocationEntities = transferDeclareCostAllocationService.lambdaQuery().in(TransferDeclareCostAllocationEntity::getMainId, ids).list();
        List<String> costAllocationId = costAllocationEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<TransferDeclareCostAllocationDetailEntity> costAllocationDetailEntities = transferDeclareCostAllocationDetailService.listByMainIds(costAllocationId);

        //b2c报关对账单
        List<String> declareReconciliationDetailIds = allocationMainEntityList.stream().map(req -> req.getDeclareReconciliationDetailId()).distinct().collect(Collectors.toList());
        List<TmsB2cDeclareReconciliationDetailEntity> tmsB2cDeclareReconciliationDetailEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(declareReconciliationDetailIds)) {
            tmsB2cDeclareReconciliationDetailEntities = tmsB2cDeclareReconciliationDetailService.listByIds(declareReconciliationDetailIds);

        }
        List<String> declareReconciliationIds = tmsB2cDeclareReconciliationDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        List<TmsB2cDeclareReconciliationEntity> tmsB2cDeclareReconciliationEntities = new ArrayList<>();
        if (CollUtil.isEmpty(declareReconciliationIds)) {
            tmsB2cDeclareReconciliationEntities = tmsB2cDeclareReconciliationService.listByIds(declareReconciliationIds);
        }
        //销售出库
        List<String> outstockDetailId = costAllocationEntities.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = new ArrayList<>();
        if (CollUtil.isNotEmpty(outstockDetailId)) {
            soOutstockDetailEntityList = FeignQuery.getByIds(SoOutstockDetailEntity.class, outstockDetailId);
        }
        List<String> soOutstockIds = soOutstockDetailEntityList.stream().map(SoOutstockDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntities = new ArrayList<>();
        if (CollUtil.isEmpty(soOutstockIds)) {
            soOutstockEntities = soOutstockFeign.listByIds(soOutstockIds);
        }

        for (TransferDeclareCostAllocationMainEntity mainEntity : allocationMainEntityList) {
            List<TransferDeclareCostAllocationEntity> costAllocationEntityList = costAllocationEntities.stream().filter(req -> req.getMainId().equals(mainEntity.getId())).collect(Collectors.toList());

            List<String> costAllocationIds = costAllocationEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
            List<TransferDeclareCostAllocationDetailEntity> costAllocationDetailEntityList = costAllocationDetailEntities.stream()
                    .filter(req -> costAllocationIds.contains(req.getMainId()))
                    .collect(Collectors.toList());

            TmsB2cDeclareReconciliationDetailEntity reconciliationDetailEntity = tmsB2cDeclareReconciliationDetailEntities.stream().filter(req -> req.getId().equals(mainEntity.getDeclareReconciliationDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(reconciliationDetailEntity)) {
                XxlJobHelper.log("b2c报关对账单详情不存在!");
                continue;
            }
            TmsB2cDeclareReconciliationEntity reconciliationEntity = tmsB2cDeclareReconciliationEntities.stream().filter(req -> req.getId().equals(reconciliationDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(reconciliationEntity)) {
                XxlJobHelper.log("b2c报关对账单不存在!");
                continue;
            }

            List<String> outDetailId = costAllocationEntityList.stream().map(req -> req.getOutstockDetailId()).distinct().collect(Collectors.toList());
            List<SoOutstockDetailEntity> collect = soOutstockDetailEntityList.stream().filter(req -> outDetailId.contains(req.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(collect)) {
                XxlJobHelper.log("销售出库详情不存在!");
                continue;
            }
            SoOutstockEntity soOutstockEntity = soOutstockEntities.stream().filter(req -> req.getId().equals(collect.get(0).getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soOutstockEntity)) {
                XxlJobHelper.log("销售出库详情不存在!");
                continue;
            }
            logisticsLargeService.generateTransferCostAllocationTable(mainEntity, costAllocationEntityList, costAllocationDetailEntityList, reconciliationEntity, reconciliationDetailEntity, soOutstockEntity);
        }
    }


    /**
     * 头程费用分摊自动生成物流大表
     */
    @XxlJob("firstMileCostAllocationToLogisticsLarge")
    public void firstMileCostAllocationToLogisticsLarge() {
        List<String> ids = logisticsLargeService.listFirstMileCostAllocationIsExists();
        List<FirstMileCostAllocationEntity> costAllocationEntityList = firstMileCostAllocationService.listByIds(ids);

        //头程费用SKU分摊信息
        List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityAllList = firstMileSkuCostAllocationService.listByMainIds(ids);
        //头程费用SKU分摊明细
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntities = firstMileSkuCostAllocationDetailService.listByMainIds(ids);

        //获取头程发货单id
        List<String> deliveryIds = costAllocationEntityList.stream().map(FirstMileCostAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        //查询头程发货单
        List<FirstMileDeliveryEntity> deliveryEntities = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        //查询头程发货单详情
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = wmsFirstMileDeliveryFeign.listDetailByMainIds(deliveryIds);

        for (FirstMileCostAllocationEntity entity : costAllocationEntityList) {
            List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList = skuCostAllocationEntityAllList.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());

            if (ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus())) {
                XxlJobHelper.log("只有已确认的单据可以生成物流大表数据");
                continue;
            }
            FirstMileDeliveryEntity deliveryEntity = deliveryEntities.stream().filter(req -> req.getId().equals(entity.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(deliveryEntity)) {
                XxlJobHelper.log("未找到关联的头程发货单信息");
                continue;
            }
            List<LogisticsLargeEntity> logisticsLargeEntities = logisticsLargeService.listByIdOutstockCode(Arrays.asList(deliveryEntity.getCode()));

            //已确认才能下推
            if (!ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
                XxlJobHelper.log(ApiError.ERROR_SMALL_BAG_NOT_CONFIRMED.msg);
                continue;
            }

            //只能下推一个实际账单
            LogisticsLargeEntity logisticsLargeActualEntity = logisticsLargeEntities.stream()
                    .filter(req -> ReconciliationBillTypeEnum.ACTUAL.getCode().equals(req.getReconciliationBillType())
                            && CharSequenceUtil.isBlank(entity.getEstimatedBillId()))
                    .findFirst().orElse(null);
            if (logisticsLargeActualEntity != null) {
                XxlJobHelper.log(ApiError.ERROR_EXISTS_LOGISTICS_LARGE.msg);
                continue;
            }
            //预估账单只能推送一个
            LogisticsLargeEntity logisticsLargeEstimatedEntity = logisticsLargeEntities.stream()
                    .filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType())
                            && CharSequenceUtil.isNotBlank(entity.getEstimatedBillId()))
                    .findFirst().orElse(null);
            if (logisticsLargeEstimatedEntity != null) {
                XxlJobHelper.log(ApiError.ERROR_EXISTS_ESTIMATED_LOGISTICS_LARGE.msg);
                continue;
            }

            //已经有实际账单不能再下推预估账单
            LogisticsLargeEntity logisticsLargeEntity = logisticsLargeEntities.stream()
                    .filter(req -> ReconciliationBillTypeEnum.ACTUAL.getCode().equals(req.getReconciliationBillType())
                            && CharSequenceUtil.isBlank(entity.getEstimatedBillId()))
                    .findFirst().orElse(null);
            if (logisticsLargeEntity != null) {
                XxlJobHelper.log(ApiError.ERROR_EXISTS_ACTUAL_NOT_ESTIMATED.msg);
                continue;
            }
            logisticsLargeService.generateFirstMileLogistics(entity,  skuCostAllocationEntityList, skuCostAllocationDetailEntities, deliveryEntity, firstMileDeliveryDetailEntities);
        }
    }
}
