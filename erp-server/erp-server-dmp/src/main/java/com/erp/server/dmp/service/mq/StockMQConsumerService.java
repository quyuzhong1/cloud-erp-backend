package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.entity.DmpWarehouseInboundRecordEntity;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.ApiPlmSyncLogService;
import com.erp.server.dmp.service.DmpWarehouseInboundRecordService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class StockMQConsumerService {

    @Resource
    private DmpWarehouseInboundRecordService dmpWarehouseInboundRecordService;

    @Resource
    private ApiPlmSyncLogService apiPlmSyncLogService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    /**
     * rocketmq 监听第三方仓库存变更
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
            selectorExpression = "gc_stock_inbound_order_tag||iml_stock_inbound_order_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-stock_inbound_order_consumer")
    public class ConsumerErpSalesOrder implements RocketMQListener<GoodcangDTO.MessageDTO> {
        @Override
        public void onMessage(GoodcangDTO.MessageDTO ext) {
            log.info("监听到第三方上架库存变化消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            Integer sendResult = ApiSendStatusEnum.FAILURE.getCode();
            String msg = "";
            try {
                Boolean process = process(ext);
                sendResult = ApiSendStatusEnum.SUCCESS.getCode();
                msg = "执行成功";
            }catch (Exception e){
                msg = e.getMessage();
                throw new RuntimeException(e);
            }finally {
                // 写入日志 执行结果
                apiPlmSyncLogService.insert(new ApiPlmSyncLogDTO(PlatformEnum.KINGDEE, ApiModuleTypeEnum.STOCK_OVERSEAS.getCode(),ext.getReceivingCode(),sendResult, msg,JSONUtil.toJsonStr(ext)));
            }
        }
        /**
         * 推送调拨到
         * @param ext
         * @return
         */
        public Boolean process(GoodcangDTO.MessageDTO ext) {
            // 查询本地是否存在
            Optional<DmpWarehouseInboundRecordEntity> inboundRecordEntityOptional = dmpWarehouseInboundRecordService.getByOrderCode(ext.getReceivingCode());
            if(inboundRecordEntityOptional.isPresent()){
                log.error("调拨单已存在，不允许重复推送：entity={}", JSONUtil.toJsonStr(ext));
                return Boolean.FALSE;
            }
            // 推送金蝶
            // 1. 金蝶接口调用 保存
            //读取配置，初始化SDK
            KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_TRANSFER_DIRECT.getCode());
            String saveId = dmpWarehouseInboundRecordService.addKingdeeTransferRecord(ext, apiUtils);
            // 保存成功后不影响消费，如果后续操作失败使用定时任务补偿
            try {
                // 3. 金蝶接口调用 提交
                Boolean submitResult = dmpWarehouseInboundRecordService.submitKingdeeTransferRecord(saveId, ext.getReceivingCode(), apiUtils);
                if(!submitResult){
                    log.error("调拨单提交失败：entity={}", JSONUtil.toJsonStr(ext));
                    return Boolean.FALSE;
                }
                // 4. 金蝶接口调用 审核
                Boolean auditResult = dmpWarehouseInboundRecordService.auditKingdeeTransferRecord(saveId, ext.getReceivingCode(), apiUtils);
                if(!auditResult){
                    log.error("调拨单审核失败：entity={}", JSONUtil.toJsonStr(ext));
                    return Boolean.FALSE;
                }
            } catch (Exception e){
                log.error("调拨单提交或审核失败：entity={}", JSONUtil.toJsonStr(ext));
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    }

}
