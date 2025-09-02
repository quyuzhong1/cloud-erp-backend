package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformFulfillOrderDTO;
import com.common.business.dto.PlatformFulfillOrderDetailDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoMultiChannelDetailEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.wms.feign.ThirdWarehouseDeliveryFeign;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoMultiChannelDetailService;
import com.erp.server.oms.service.SoMultiChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 下载FBA货件消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FULFILL_ORDER_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FULFILL_ORDER_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FULFILL_ORDER_TO_OMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewSoMultilChannelConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private SoMultiChannelService soMultiChannelService;
    @Resource
    private SoMultiChannelDetailService soMultiChannelDetailService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;
    @Override
    public String getBizName() {
        return "多渠道订单";
    }

    @Override
    public void handle(String data) {
        System.out.println(data);
        PlatformFulfillOrderDTO bean = JSONUtil.toBean(data, PlatformFulfillOrderDTO.class);
        SoMultiChannelEntity soMultiChannelEntity = soMultiChannelService.getByDeliveryCode(bean.getCode());
        List<PlatformFulfillOrderDetailDTO> detailList = bean.getDetailList();
        if (Objects.isNull(soMultiChannelEntity)){
            //订单不存在
            log.info("订单不存在，订单编号：{}",bean.getCode());
            return;
        }
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新多渠道订单发货状态:【{}】改为【{}】", SoB2cBillStatusEnum.getName(soMultiChannelEntity.getDeliveryStatus()), SoB2cBillStatusEnum.ENUM_SHIPPED.getName()), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelEntity.getId(), "更新亚马逊多渠道订单");
        //订单状态和发货状态
        soMultiChannelEntity.setDeliveryTime(bean.getDeliveryTime());
        soMultiChannelEntity.setTrackNo(bean.getTrackNo());
        soMultiChannelEntity.setBillStatus(bean.getOrderStatus());
        soMultiChannelEntity.setDeliveryStatus(CharSequenceUtil.isNotBlank(bean.getDeliveryStatus())?bean.getDeliveryStatus():"");
        soMultiChannelService.updateById(soMultiChannelEntity);

        List<SoMultiChannelDetailEntity> detailEntityList = soMultiChannelDetailService.listByMainIds(Collections.singletonList(soMultiChannelEntity.getId()));
        if ("CANCELLED".equalsIgnoreCase(bean.getOrderStatus()) || "CANCELLED_BY_FULFILLER".equalsIgnoreCase(bean.getDeliveryStatus()) || "CANCELLED_BY_SELLER".equalsIgnoreCase(bean.getDeliveryStatus())){
            //订单已取消
            soMultiChannelService.deliveryIntercept(soMultiChannelEntity, false, true);
        }else {
            //更新发货数量
            updateSoMultiChannelDetail(detailEntityList, detailList);
            if (CharSequenceUtil.isNotBlank(soMultiChannelEntity.getSoId()) && "SHIPPED".equalsIgnoreCase(bean.getDeliveryStatus())){
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
    }

    private void updateSoMultiChannelDetail(List<SoMultiChannelDetailEntity> detailEntityList, List<PlatformFulfillOrderDetailDTO> detailList) {
        if (CollUtil.isEmpty(detailList)){
            return;
        }
        //更新发货数量
        detailEntityList.forEach(e -> {
            int deliveryQty = detailList.stream().filter(f -> f.getFnSku().equals(e.getFnSku()) && e.getPlatformSkuNo().equals(f.getMsku())).mapToInt(PlatformFulfillOrderDetailDTO::getDeliveryQty).sum();
            e.setDeliveryQty(deliveryQty);
            soMultiChannelDetailService.lambdaUpdate().set(SoMultiChannelDetailEntity::getDeliveryQty,deliveryQty).eq(SoMultiChannelDetailEntity::getId,e.getId()).update();
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新多渠道订单明细【{}】发货数量:【{}】改为【{}】", e.getSkuNo(),e.getDeliveryQty(), deliveryQty), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), e.getMainId(), "更新亚马逊多渠道订单");
        });
    }

}