package com.erp.server.sys.rocketmq.consumer;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.plm.entity.NoticeMessageRecordEntity;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.model.sys.vo.SendThirdNoticeConsumerDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.service.ThirdNoticePushRecordService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC,
        selectorExpression = "sys_send_third_notice_tag",
        consumerGroup = RocketMqConsumerGroup.SYS_SEND_THIRD_NOTICE_CONSUMER)
public class SendThirdNoticeConsumerService implements RocketMQListener<SendThirdNoticeConsumerDTO> {
    /*
    //------plm------
    新建产品  com.erp.server.plm.service.impl.NoticeMessageServiceImpl.newProductNotice  productInfo
    产品信息变更 com.erp.server.plm.service.impl.ProductDetailServiceImpl.handleProductChangeNotification productDetail
    模具审核 com.erp.server.plm.service.impl.MouldInfoServiceImpl.sendApproveNotice  mouldinfo
    模具提交 com.erp.server.plm.service.impl.MouldInfoServiceImpl.submit
    模具创建 com.erp.server.plm.service.impl.MouldInfoServiceImpl.add
    返还确认 com.erp.server.plm.service.impl.MouldInfoServiceImpl.returnConfirm
    返还达量 com.erp.server.plm.service.impl.MouldRefCalcQtyServiceImpl.calcRefundQty MouldRefCalcQty

    //------wms------
    质检通知 -新品 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcResultMsg
    质检通知 -老品 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcResultMsg
    首次质检 -产品尺寸变更 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcBackFillPackaging
    质检通知 com.erp.server.wms.schedule.CfgSettingJob.fsQcNotice
    仓位补货通知 com.erp.server.wms.schedule.CfgSettingJob.fsWlrNotice
    要货申请待处理 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_WAITHANDLE_NOTICE
    要货申请处理中 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_HANDLEING_NOTICE
    要货申请已装箱 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_PACKING_NOTICE
    要货申请已完成 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_NOTICE
    头程发货单待处理 com.erp.server.wms.service.impl.FirstMileDeliveryServiceImpl.submit
    要货申请变更单提交 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_CHANGE_SUBMIT_NOTICE
    要货申请变更单审核 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_CHANGE_APPROVE_NOTICE
    装箱完成通知 com.erp.server.wms.service.impl.PackingTaskServiceImpl.sendNoticeMsg

    //------mrp------
    确认发货建议 com.erp.server.mrp.schedule.CfgNoticeJob.sendMrpNotice
    生成发货建议 com.erp.server.mrp.schedule.CfgNoticeJob.sendMrpNotice

    //------tms------
    在途异常 com.erp.server.tms.schedule.FmLogisticWarnJob.sendFmLogisticWarnJob
    渠道更换 com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.updateChannel  / com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.batchUpdateChannel
    备案通知 com.erp.server.tms.service.impl.ProductRegistrationServiceImpl.sendMsgWhenNotRegistration
    组包预报生成【新增】
    物流单下单成功【新增】

*/


    @Resource
    private FsService fsService;
    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(SendThirdNoticeConsumerDTO dto) {
        log.info("SendThirdNoticeConsumerService 开始");
        LocalDateTime now = LocalDateTime.now();
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
            thirdNoticePushRecordService.lambdaUpdate()
                    .set(ThirdNoticePushRecordEntity::getStatus, ThirdNoticePushRecordStatusEnum.FAILED.getCode())
                    .set(ThirdNoticePushRecordEntity::getSendTime,now)
                    .eq(ThirdNoticePushRecordEntity::getId, messageId)
                    .update();
        }
        log.info("SendThirdNoticeConsumerService 结束");
    }
}
