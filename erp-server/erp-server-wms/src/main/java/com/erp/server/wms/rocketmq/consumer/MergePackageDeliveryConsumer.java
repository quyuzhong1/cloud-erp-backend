package com.erp.server.wms.rocketmq.consumer;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.utils.CollectionUtils;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC, selectorExpression = "async_merge_package_delivery_tag", consumerGroup = RocketMqConsumerGroup.ASYNC_MERGE_PACKAGE_DELIVERY_CONSUMER)
public class MergePackageDeliveryConsumer implements RocketMQListener<String> {
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private CommonService commonService;
    @Resource
    private OperateLogService operateLogService;

    @Override
    public void onMessage(String soId) {
        //查询发货单
        List<SoB2cDeliveryEntity> deliveryEntities = soB2cDeliveryService.listBySourceIds(Arrays.asList(soId));
        if (CollectionUtil.isEmpty(deliveryEntities)) {
            return;
        }

        //记录需要虚假发货的订单id
        Boolean flag = soB2cFeign.checkPlatformShipOrder(soId);
        if (flag) {
            List<String> soDeliveryIds = deliveryEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
            //调用第三方平台SDK发货
            BatchResultDTO resultDTO = soB2cDeliveryService.falseDelivery(soDeliveryIds.get(0));
            if (!resultDTO.getSuccess()) {
                String type = SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode();
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(type);
                addError.setParamJson(soId);
                addError.setReturnJson(resultDTO.toString());
                addError.setMainId(soId);
                addError.setMessage(resultDTO.getMsg());
                soB2cFeign.addSoB2cError(addError);
                return;
            }
        }
        SoB2cDeliveryEntity deliveryEntity = deliveryEntities.get(0);

        //将发货状态更新为已发货
        deliveryEntity.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getCode());
        //获取一个当前时间当作发货时间
        LocalDateTime deliveryTime = LocalDateTime.now();
        deliveryEntity.setDeliveryTime(deliveryTime);

        //将发货状态更新为已发货
        if (!soB2cDeliveryService.updateById(deliveryEntity)) {
            throw new ServiceException("发货单更新失败");
        }

        //修改订单状态已发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(soId));
        updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        updateDeliveryTimeDTO.setDeliveryTime(LocalDateTime.now());
        soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);

        //出库
        soB2cDeliveryService.generateB2cSoOutstock(deliveryEntities.get(0));

        String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", commonService.getUserInfo().getUserName(), "组包称重", deliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), deliveryEntity.getId(), "组包称重");
    }
}
