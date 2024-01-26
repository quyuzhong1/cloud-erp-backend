package com.erp.server.dmp.service.mq;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.constant.RedisCacheConstants;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.server.dmp.service.AmzReportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 亚马逊报告消费服务
 *
 * @Author Jim
 * @Date 2024/01/20
 **/
@Slf4j
@Component
public class MQConsumerAmzReportService {

    @Resource
    private AmzReportTaskService amzReportTaskService;

    /**
     * 报告步骤1
     * 报告创建消费处理
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
            selectorExpression = "amz_report_create_tag",
            consumerGroup = RocketMqConsumerGroup.SYNC_AMZ_REPORT_CREATE)
    public class ConsumerAmzReportCreate implements RocketMQListener<AmzReportTaskEntity> {
        @Override
        public void onMessage(AmzReportTaskEntity entity) {
            try {
                log.info("【亚马逊报告】步骤1:报告创建消费处理：entity={}", JSONUtil.toJsonStr(entity));
                // 当前分组报告处理中锁key
                String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, entity.getShopId(), entity.getReportType());
                // 报告创建处理
                amzReportTaskService.consumerReportCreate(reportRedissonKey, entity);
            } catch (Exception e) {
                amzReportTaskService.updateErrorMsgAndCount(entity, ExceptionUtil.stacktraceToString(e, 2000), entity.getCreatedRetryCount() + 1, null, null, null);
                throw e;
            }
        }
    }

    /**
     * 报告步骤2
     * 报告查询消费处理
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
            selectorExpression = "amz_report_query_tag",
            consumerGroup = RocketMqConsumerGroup.SYNC_AMZ_REPORT_QUERY)
    public class ConsumerAmzReportQuery implements RocketMQListener<AmzReportTaskEntity> {
        @Override
        public void onMessage(AmzReportTaskEntity entity) {
            try {
                log.info("【亚马逊报告】步骤2:报告查询消费处理：entity={}", JSONUtil.toJsonStr(entity));
                // 当前分组报告处理中锁key
                String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, entity.getShopId(), entity.getReportType());
                // 报告查询处理
                amzReportTaskService.consumerReportQuery(reportRedissonKey, entity);
            } catch (Exception e) {
                amzReportTaskService.updateErrorMsgAndCount(entity, ExceptionUtil.stacktraceToString(e, 2000), null, entity.getQueryRetryCount() + 1, null, null);
                throw e;
            }
        }
    }

    /**
     * 报告步骤3
     * 报告下载消费处理
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
            selectorExpression = "amz_report_download_tag",
            consumerGroup = RocketMqConsumerGroup.SYNC_AMZ_REPORT_DOWNLOAD)
    public class ConsumerAmzReportDownload implements RocketMQListener<AmzReportTaskEntity> {
        @Override
        public void onMessage(AmzReportTaskEntity entity) {
            try {
                log.info("【亚马逊报告】步骤3:报告下载消费处理：entity={}", JSONUtil.toJsonStr(entity));
                // 当前分组报告处理中锁key
                String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, entity.getShopId(), entity.getReportType());
                // 报告下载处理
                amzReportTaskService.consumerReportDownload(reportRedissonKey, entity);
            } catch (Exception e) {
                amzReportTaskService.updateErrorMsgAndCount(entity, ExceptionUtil.stacktraceToString(e, 2000),  null, null,entity.getDownloadRetryCount() + 1, null);
                throw e;
            }
        }
    }

    /**
     * 报告步骤4
     * 报告解析消费处理
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
            selectorExpression = "amz_report_parse_tag",
            consumerGroup = RocketMqConsumerGroup.SYNC_AMZ_REPORT_PARSE)
    public class ConsumerAmzReportParse implements RocketMQListener<AmzReportTaskEntity> {
        @Override
        @DataIdempotent(keyIdName = "entity.redissonKey", waitTime = 120)
        public void onMessage(AmzReportTaskEntity entity) {
            try {
                log.info("【亚马逊报告】步骤4：报告解析消费处理：entity={}", JSONUtil.toJsonStr(entity));
                // 当前分组报告处理中锁key
                String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, entity.getShopId(), entity.getReportType());
                // 报告解析处理
                amzReportTaskService.consumerReportParse(reportRedissonKey, entity);
            } catch (Exception e) {
                amzReportTaskService.updateErrorMsgAndCount(entity, ExceptionUtil.stacktraceToString(e, 2000), null, null, null, entity.getParseRetryCount() + 1);
                throw e;
            }
        }
    }

    /**
     * 报告步骤4
     * 报告解析消费处理
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
            selectorExpression = "amz_report_direct_query_tag",
            consumerGroup = RocketMqConsumerGroup.SYNC_AMZ_REPORT_DIRECT_QUERY)
    public class ConsumeAmzReportDirectQuery implements RocketMQListener<AmzReportTaskEntity> {
        @Override
        @DataIdempotent(keyIdName = "entity.redissonKey", waitTime = 120)
        public void onMessage(AmzReportTaskEntity entity) {
            try {
                log.info("【亚马逊报告】步骤4：报告解析消费处理：entity={}", JSONUtil.toJsonStr(entity));
                // 当前分组报告处理中锁key
                String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, entity.getShopId(), entity.getReportType());
                // 报告解析处理
                amzReportTaskService.consumerReportDirectQuery(reportRedissonKey, entity);
            } catch (Exception e) {
                amzReportTaskService.updateErrorMsgAndCount(entity, ExceptionUtil.stacktraceToString(e, 2000), null, entity.getQueryRetryCount() + 1, null, null);
                throw e;
            }
        }
    }
}
