package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.handler.AbstractPlatformPullConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 下载平台入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_outbound_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_outbound_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOutboundConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformPullConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId,String msg) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity,msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformOutboundDTO dto = JSONUtil.toBean(ext.toString(), PlatformOutboundDTO.class);
        if(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dto.getOrderStatus())){
            //TODO 已发货自动生成销售出库单并自动审核,扣减可用库存
        }
        return ApiResult.success();
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity,String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
        warnMsgInfo.setTitle(StrUtil.format("平台入库消息消费失败，来源平台:{},目标平台:{}",dmpPullTaskEntity.getSourcePlatformName(),dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(StringUtils.isBlank(msg)?"":msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
