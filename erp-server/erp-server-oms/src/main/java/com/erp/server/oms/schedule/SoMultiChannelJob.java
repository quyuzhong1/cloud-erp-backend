package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.DmpPlatformSoDeliveryEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoMultiChannelDetailEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.oms.enums.CreateStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.wms.feign.ThirdWarehouseDeliveryFeign;
import com.erp.server.oms.service.InvoiceInfoService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoMultiChannelService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 多渠道同步发货状态
 */
@Component
@Slf4j
public class SoMultiChannelJob {

    @Resource
    private SoMultiChannelService soMultiChannelService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;
    /**
     * 查询多渠道订单发货状态
     * @return
     */
    @XxlJob("queryMultiChannelDeliveryStatus")
    public ReturnT<String> queryMultiChannelDeliveryStatus() {
        XxlJobHelper.log("查询多渠道订单发货状态");
        List<SoMultiChannelEntity> soMultiChannelEntities = soMultiChannelService.queryMultiChannelDeliveryStatus();
        if (CollUtil.isNotEmpty(soMultiChannelEntities)){
            List<String> deliveryCodeList = soMultiChannelEntities.stream().map(SoMultiChannelEntity::getDeliveryCode).collect(Collectors.toList());
            List<DmpPlatformSoDeliveryEntity> list = FeignQuery.list(FeignQuery.create(DmpPlatformSoDeliveryEntity.class)
                    .in(DmpPlatformSoDeliveryEntity::getCode, deliveryCodeList));
            soMultiChannelEntities.forEach(soMultiChannelEntity -> {
                //获取最近一条记录
                list.stream().filter(e -> e.getCode().equals(soMultiChannelEntity.getDeliveryCode())).findFirst().ifPresent(e -> {
                    operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新多渠道订单发货状态:【{}】改为【{}】", SoB2cBillStatusEnum.getName(soMultiChannelEntity.getDeliveryStatus()), SoB2cBillStatusEnum.ENUM_SHIPPED.getName()), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelEntity.getId(), "更新亚马逊多渠道订单");
                    //订单状态和发货状态
                    soMultiChannelEntity.setDeliveryTime(e.getDeliveryTime());
                    soMultiChannelEntity.setTrackNo(e.getTrackNo());
                    soMultiChannelEntity.setBillStatus(e.getOrderStatus());
                    soMultiChannelEntity.setDeliveryStatus(CharSequenceUtil.isNotBlank(e.getDeliveryStatus())?e.getDeliveryStatus():"");

                    if ("CANCELLED".equalsIgnoreCase(e.getOrderStatus()) || "CANCELLED_BY_FULFILLER".equalsIgnoreCase(e.getDeliveryStatus()) || "CANCELLED_BY_SELLER".equalsIgnoreCase(e.getDeliveryStatus())){
                        soMultiChannelEntity.setCreateStatus(CreateStatusEnum.CANCEL.getCode());
                        soMultiChannelService.updateById(soMultiChannelEntity);
                        //订单已取消
                        soMultiChannelService.deliveryIntercept(soMultiChannelEntity, false, true, "订单已取消");
                    }else {
                        soMultiChannelService.updateById(soMultiChannelEntity);
                        if (CharSequenceUtil.isNotBlank(soMultiChannelEntity.getSoId()) && "SHIPPED".equalsIgnoreCase(e.getDeliveryStatus())){
                            SoB2cEntity entity = soB2cService.getById(soMultiChannelEntity.getSoId());
                            if (Objects.nonNull(entity) && !SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(entity.getBillStatus())){
                                operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新销售订单发货状态:【{}】改为【{}】", SoB2cBillStatusEnum.getName(soMultiChannelEntity.getDeliveryStatus()), SoB2cBillStatusEnum.ENUM_SHIPPED.getName()), ModuleTypeEnum.SO_B2C.getCode(), soMultiChannelEntity.getSoId(), "更新亚马逊多渠道订单");
                                soB2cService.lambdaUpdate().set(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_SHIPPED.getCode()).eq(SoB2cEntity::getId, entity.getId()).update();

                            }
                            //第三方发货单更新状态
                            ThirdWarehouseDeliveryEntity thirdWarehouseDelivery = thirdWarehouseDeliveryFeign.getLatestBySoId(soMultiChannelEntity.getSoId());
                            if (Objects.nonNull(thirdWarehouseDelivery) && !SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getCode().equals(thirdWarehouseDelivery.getStatus())){
                                thirdWarehouseDelivery.setStatus(SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getCode());
                                thirdWarehouseDeliveryFeign.update(thirdWarehouseDelivery);
                            }
                        }
                    }

                });


            });
        }
        XxlJobHelper.log("查询多渠道订单发货状态完成");
        return ReturnT.SUCCESS;
    }

}
