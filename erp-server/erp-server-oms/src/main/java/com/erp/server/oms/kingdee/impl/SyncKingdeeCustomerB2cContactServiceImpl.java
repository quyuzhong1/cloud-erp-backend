package com.erp.server.oms.kingdee.impl;

import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.entity.CustomerB2cContactEntity;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerB2cContactService;
import com.erp.server.oms.service.CustomerB2cAddressService;
import com.erp.server.oms.service.CustomerB2cContactService;
import com.erp.server.oms.service.CustomerB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 同步客户到金蝶
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeCustomerB2cContactServiceImpl implements SyncKingdeeCustomerB2cContactService {
    @Resource
    private CustomerB2cService customerB2cService;

    @Resource
    private CustomerB2cContactService customerB2cContactService;

    @Resource
    private CustomerB2cAddressService customerB2cAddressService;

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void syncDataToKingdee(CustomerB2cContactEntity entity, String operate) {

        //更新同步状态为待同步
        customerB2cContactService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.TO_BE_SYNC.getCode(),"",operate);

        //客户地址信息
        List<CustomerAddressDTO.ViewDTO> viewDTOS = customerB2cAddressService.listByMainId(entity.getMainId());
        //客户信息
        CustomerB2cEntity customerB2cEntity = customerB2cService.getById(entity.getMainId());
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //客户联系人
        resultMap.put("code", entity.getCode());
        //联系人名称
        resultMap.put("person", entity.getPerson());
        //职位
        resultMap.put("position", entity.getPosition());
        //联系电话
        resultMap.put("telNumber", entity.getTelNumber());
        //电子邮箱
        resultMap.put("email", entity.getEmail());
        //是否默认
        resultMap.put("isDefault", entity.getIsDefault());
        //禁用状态
        resultMap.put("disabled", entity.getDisabled());
        //地址编号
        CustomerAddressDTO.ViewDTO viewDTO = viewDTOS.stream().filter(req -> req.getPerson().equals(entity.getPerson())).findFirst().orElse(new CustomerAddressDTO.ViewDTO());
        resultMap.put("addressCode", viewDTO.getCode());
        resultMap.put("address", viewDTO.getAddress());
        //客户编号
        resultMap.put("customerCode", customerB2cEntity.getCode());

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_CUSTOMER_CONTACT_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return customerB2cContactService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.IN_SYNC.getCode(),"", operate);
            }
            return Boolean.TRUE;
        });
    }
}
