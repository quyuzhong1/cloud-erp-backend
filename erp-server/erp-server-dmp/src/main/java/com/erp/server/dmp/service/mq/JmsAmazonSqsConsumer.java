package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONObject;
import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import com.common.business.constant.BusinessCommonConstants;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.ReportHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.Message;

/**
 * 亚马逊SQS消息监听
 */
@Service
@Slf4j
public class JmsAmazonSqsConsumer {

    @Resource
    private MongoService mongoService;
    @Resource
    private ReportHandleService reportHandleService;

    @Value("${spring.cloud.nacos.discovery.namespace}")
    private String namespace;

    /**
     * 监听接收消息
     * 如果有多个Factory 需要手动指定
     */
    @JmsListener(destination = "erpNotifications", containerFactory = "jmsListenerContainerFactory")
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void consumerListener(Message message) throws Exception {
        SQSTextMessage textMessage = (SQSTextMessage) message;
        log.debug("接收到亚马逊SQS通知:{}", textMessage.getText());
        if (BusinessCommonConstants.hasProfile("dev") || BusinessCommonConstants.DEV.equalsIgnoreCase(namespace)){
            // 开发环境暂时过滤
            return ;
        }

        // 处理报告完成队列
        if ("REPORT_PROCESSING_FINISHED".equalsIgnoreCase(new JSONObject(textMessage.getText()).getStr("notificationType"))) {
            reportHandleService.handlerNotifications(textMessage);
        }

        // 环境处理
        if (!BusinessCommonConstants.hasProfile("dev") && BusinessCommonConstants.DEV.equalsIgnoreCase(namespace)){
            //如果设置的是客户端确认模式(Session.CLIENT_ACKNOWLEDGE)，调用acknowledge()删除sqs消息。
            message.acknowledge();
        }

    }


}