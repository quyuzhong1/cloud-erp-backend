package com.erp.server.dmp.service.mq;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.pull.service.dmp.*;
import com.erp.server.dmp.service.ApiPlmSyncLogService;
import com.erp.server.dmp.service.DmpWarehouseInboundItemService;
import com.erp.server.dmp.service.DmpWarehouseInboundRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Component
public class StockMQConsumerService {

    @Resource
    private DmpWarehouseInboundRecordService dmpWarehouseInboundRecordService;

    @Resource
    private ApiPlmSyncLogService apiPlmSyncLogService;

    /**
     * rocketmq 监听第三方仓库存变更
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
            selectorExpression = "gc_stock_inbound_order_tag",
            consumerGroup = "${spring.profiles.active}-stock_inbound_order_consumer")
    public class ConsumerErpSalesOrder implements RocketMQListener<GoodcangDTO.MessageDTO> {
        @Override
        public void onMessage(GoodcangDTO.MessageDTO ext) {
            log.info("监听到第三方上架库存变化消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            Integer sendResult = ApiSendStatusEnum.FAILURE.getCode();
            String msg = "";
            try {
                dmpWarehouseInboundRecordService.process(ext);
                sendResult = ApiSendStatusEnum.SUCCESS.getCode();
                msg = "执行成功";
            }catch (Exception e){
                msg = e.getCause().toString();
                throw new RuntimeException(e);
            }finally {
                // 写入日志 执行结果
                apiPlmSyncLogService.insert(new ApiPlmSyncLogDTO(PlatformEnum.KINGDEE, ApiModuleTypeEnum.STOCK_OVERSEAS.getCode(),ext.getReceivingCode(),sendResult, msg,JSONUtil.toJsonStr(ext)));
            }
        }
    }



}
