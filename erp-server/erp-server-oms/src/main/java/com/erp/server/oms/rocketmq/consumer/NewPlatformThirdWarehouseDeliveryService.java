package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.dto.ThirdWarehouseCreateOutboundPushDTO;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.impl.SoB2cServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_THIRD_WAREHOUSE_ERP_TOPIC,
        selectorExpression = "${spring.cloud.nacos.discovery.namespace}-erp_third_warehouse_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-erp_dmp_group",
        consumeMode = ConsumeMode.ORDERLY)
public class NewPlatformThirdWarehouseDeliveryService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private SoB2cServiceImpl soB2cService;
    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Override
    public String getBizName() {
        return "创建三方仓出库单";
    }

    @Override
    public void handle(String data) {
        log.warn("创建订单请求参数：{}", data);
        JSONObject jsonObject = JSONUtil.parseObj(data);
        ThirdWarehouseCreateOutboundPushDTO body = JSONUtil.toBean(jsonObject, ThirdWarehouseCreateOutboundPushDTO.class);

        log.warn("B2C订单【{}】下出库单，第三方仓【{}】，物流单【{}】", body.getEntity().getCode(), body.getWarehouseId(), body.getLogisticsEntity().getCode());
        try {
            //下出库单命令
            soB2cService.thirdWarehouseCreateOutStock(body.getEntity(), body.getWarehouseId(), body.getWarehouseManageType(), body.getLogisticsEntity(), body.getOverseasProviderWarehouse(), body.getDetailList(), body.getNewChannelId());
        } catch (Exception e) {
            log.error("B2C订单【{}】下出库单异常>>>{}", body.getEntity().getCode(), e.getMessage());
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
            addError.setParamJson("");
            addError.setReturnJson("");
            addError.setMainId(body.getEntity().getId());
            addError.setMessage(e.getMessage());
            soB2cErrorService.add(addError);
            //失败还原订单状态
            soB2cService.updateBillStatus(body.getEntity().getId(), SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION);
        }
    }
}
