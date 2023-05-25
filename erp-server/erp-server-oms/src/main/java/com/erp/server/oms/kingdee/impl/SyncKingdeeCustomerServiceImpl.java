package com.erp.server.oms.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerService;
import com.erp.server.oms.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 同步客户到金蝶
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeCustomerServiceImpl implements SyncKingdeeCustomerService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void syncDataToKingdee(CustomerInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id",entity.getId());
        //仓库名称
        resultMap.put("name",entity.getName());
     /*   //金蝶编号
        resultMap.put("code",entity.getKingdeeWarehouseCode());
        //仓库组织
        resultMap.put("orgId",entity.getOrgId());
        //仓库地址
        resultMap.put("address",entity.getAddress());
        //仓库电话
        resultMap.put("tel",entity.getContactTelNumber());
        //是否禁用
        resultMap.put("disabled",entity.getDisabled());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //审核未通过、非反审核不推送
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus().getStatus()) && !SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            return;
        }

        //仓库类型
        DictBasicEntity type = dictBasicService.getById(entity.getTypeId());
        if (ObjectUtils.isNotEmpty(type)) {
            resultMap.put("type",type.getValue());
        }
        //仓库负责人
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getChargeId());
        if (ObjectUtils.isNotEmpty(findUserDTO)) {
            resultMap.put("chargeCode",findUserDTO.getCode());
        }*/

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_CUSTOMER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return customerInfoService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });
    }
}
