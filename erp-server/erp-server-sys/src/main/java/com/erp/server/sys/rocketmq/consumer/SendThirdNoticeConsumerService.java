package com.erp.server.sys.rocketmq.consumer;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import com.erp.model.sys.vo.SendThirdNoticeConsumerDTO;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.service.ThirdNoticePushRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC,
        selectorExpression = "sys_send_third_notice_tag",
        consumerGroup = RocketMqConsumerGroup.SYS_SEND_THIRD_NOTICE_CONSUMER)
public class SendThirdNoticeConsumerService implements RocketMQListener<SendThirdNoticeConsumerDTO> {

    @Resource
    private FsService fsService;
    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(SendThirdNoticeConsumerDTO dto) {
        log.info("SendThirdNoticeConsumerService 开始");
        LocalDateTime now = LocalDateTime.now();
        try {
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(dto);
            //当发送成功后
            if (Boolean.TRUE.equals(sendResult)) {
                String messageId = dto.getMessageId();
                thirdNoticePushRecordService.lambdaUpdate()
                        .set(ThirdNoticePushRecordEntity::getStatus, ThirdNoticePushRecordStatusEnum.SUCCESS.getCode())
                        .set(ThirdNoticePushRecordEntity::getSendTime,now)
                        .eq(ThirdNoticePushRecordEntity::getId, messageId)
                        .update();
            }else {
                String messageId = dto.getMessageId();
                ThirdNoticePushRecordEntity record = thirdNoticePushRecordService.getById(messageId);
                String errorReason = record.getErrorReason();
                if(StringUtils.isBlank(errorReason)){
                    errorReason = "发送消息失败";
                }
                thirdNoticePushRecordService.lambdaUpdate()
                        .set(ThirdNoticePushRecordEntity::getStatus, ThirdNoticePushRecordStatusEnum.FAILED.getCode())
                        .set(ThirdNoticePushRecordEntity::getSendTime,now)
                        .set(ThirdNoticePushRecordEntity::getErrorReason,errorReason)
                        .eq(ThirdNoticePushRecordEntity::getId, messageId)
                        .update();
            }
        }catch(Exception e) {
            String messageId = dto.getMessageId();
            thirdNoticePushRecordService.lambdaUpdate()
                    .set(ThirdNoticePushRecordEntity::getStatus, ThirdNoticePushRecordStatusEnum.FAILED.getCode())
                    .set(ThirdNoticePushRecordEntity::getSendTime,now)
                    .set(ThirdNoticePushRecordEntity::getErrorReason, JSONUtil.toJsonStr(e))
                    .eq(ThirdNoticePushRecordEntity::getId, messageId)
                    .update();
        }
        log.info("SendThirdNoticeConsumerService 结束");
    }
}
