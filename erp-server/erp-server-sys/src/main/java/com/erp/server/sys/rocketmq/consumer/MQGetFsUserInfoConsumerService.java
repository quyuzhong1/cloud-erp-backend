package com.erp.server.sys.rocketmq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformCityDictDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpFeishuUserInfoEntity;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 飞书获取单个审批实例详情
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FS_USER_TO_SYS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FS_USER_TO_SYS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FS_USER_TO_SYS_GROUP)
public class MQGetFsUserInfoConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private FsUseInfoConsumerService fsUseInfoConsumerService;


    @Override
    public String getBizName() {
        return "飞书获取员工信息";
    }

    @Override
    public void handle(String data) {
        fsUseInfoConsumerService.handle(data);
    }
}