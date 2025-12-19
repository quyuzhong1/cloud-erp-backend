package com.erp.server.msg.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.core.utils.BeanMapper;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeMessageTypeEnum;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import com.erp.model.sys.vo.SendThirdNoticeConsumerDTO;
import com.erp.rpc.sys.feign.ThirdNoticePushRecordFeign;
import com.erp.server.msg.config.MsgContext;
import com.erp.server.msg.model.MsgResultVO;
import com.erp.server.msg.model.MsgSendChannelWrapParam;
import com.erp.server.msg.model.NoticeMsgWrapInfoDTO;
import com.erp.server.msg.service.BaseMessageSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
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
@RocketMQMessageListener(topic = RocketMqTopic.SEND_THIRD_NOTICE_TOPIC,
        selectorExpression = "send_third_notice_tag",
        consumerGroup = RocketMqConsumerGroup.SEND_THIRD_NOTICE_CONSUMER)
public class SendThirdNoticeConsumerService implements RocketMQListener<SendThirdNoticeConsumerDTO> {

    @Resource
    private MsgContext msgContext;
    @Resource
    private ThirdNoticePushRecordFeign thirdNoticePushRecordFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(SendThirdNoticeConsumerDTO dto) {
        log.info("SendThirdNoticeConsumerService 开始");
        LocalDateTime now = LocalDateTime.now();
        try {
            //发送消息的结果
            MsgSendChannelWrapParam noticeMsgInfo = new MsgSendChannelWrapParam();
            NoticeMsgWrapInfoDTO noticeMsgWrapInfoDTO = new NoticeMsgWrapInfoDTO();
            BeanMapper.copy(dto,noticeMsgWrapInfoDTO);
            noticeMsgWrapInfoDTO.setNoticeMessageTypeEnum(NoticeMessageTypeEnum.ACTION_CARD);
            noticeMsgInfo.setNoticeMsgWrapInfoDTO(noticeMsgWrapInfoDTO);
            MsgResultVO<T> sendResult = msgContext.sendByFeishu(noticeMsgInfo);

            //当发送成功后
            if (sendResult.isSuccess()) {
                log.info("sendMessage 当发送成功");
                ThirdNoticePushRecordEntity entity = dto.getThirdNoticePushRecordEntity();
                entity.setStatus(ThirdNoticePushRecordStatusEnum.SUCCESS.getCode());
                entity.setSendTime(now);
                thirdNoticePushRecordFeign.updateStatusById(entity);
            }else {
                log.info("sendMessage 当发送失败");

                ThirdNoticePushRecordEntity entity = dto.getThirdNoticePushRecordEntity();
                entity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
                entity.setSendTime(now);
                String errorReason = entity.getErrorReason();
                if(StringUtils.isBlank(errorReason)){
                    errorReason = "发送消息失败";
                }
                entity.setErrorReason(errorReason);
                thirdNoticePushRecordFeign.updateStatusById(entity);

            }
        }catch(Exception e) {
            log.error("sendMessage 发送异常 ",e);

            ThirdNoticePushRecordEntity entity = dto.getThirdNoticePushRecordEntity();
            entity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
            entity.setSendTime(now);
            entity.setErrorReason(JSONUtil.toJsonStr(e));
            thirdNoticePushRecordFeign.updateStatusById(entity);

        }
        log.info("SendThirdNoticeConsumerService 结束");
    }
}
