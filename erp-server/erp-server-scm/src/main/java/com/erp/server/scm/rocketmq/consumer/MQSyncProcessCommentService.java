package com.erp.server.scm.rocketmq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveTypeEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.erp.model.workflow.entity.CfgApproveSyncEntity;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.workflow.feign.ApproveSyncRecordFeign;
import com.erp.server.scm.service.ModuleOperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "${spring.cloud.nacos.discovery.namespace}-scm_workflow_sync_fs_comment_topic",
        selectorExpression = "workflow_sync_fs_instance_tag",
        consumerGroup = RocketMqConsumerGroup.WORKFLOW_SYNC_FS_INSTANCE_CONSUMER)
public class MQSyncProcessCommentService implements RocketMQListener<CfgApproveSyncDTO.SyncFsCommentToMqDTO> {
    @Resource
    private ModuleOperateLogService operateLogService;
    @Resource
    private ApproveSyncRecordFeign approveSyncRecordFeign;

    @Override
    public void onMessage(CfgApproveSyncDTO.SyncFsCommentToMqDTO commentToMqDTO) {
        log.info("scm MQSyncProcessCommentService 开始");
        CfgApproveSyncDTO.SyncFsProcessToMqDTO dto = commentToMqDTO.getSyncFsProcessToMqDTO();

        ApproveSyncRecordEntity approveSyncRecordEntity = commentToMqDTO.getApproveSyncRecordEntity();
        if(Objects.isNull(approveSyncRecordEntity)){
            CfgApproveSyncEntity cfgApproveSyncEntity = dto.getCfgApproveSyncEntity();

            //记录一条错误记录
            approveSyncRecordEntity = new ApproveSyncRecordEntity();
            approveSyncRecordEntity.setCfgApproveSyncId(cfgApproveSyncEntity.getId());
            approveSyncRecordEntity.setNoticeType(ApproveSyncRecordNoticeTypeEnum.SYNC_COMMENT.getCode());
            approveSyncRecordEntity.setBusinessType(cfgApproveSyncEntity.getBusinessType());
            approveSyncRecordEntity.setBusinessCode(dto.getBusinessCode());
            approveSyncRecordEntity.setNoticeMethod(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
            approveSyncRecordEntity.setSendTime(LocalDateTime.now());
            approveSyncRecordEntity.setTitle(cfgApproveSyncEntity.getTitle());
            approveSyncRecordEntity.setStatus(ApproveSyncRecordStatusEnum.FAILED.getCode());
            approveSyncRecordEntity.setNoticeNode(CfgApproveNoticeNoticeTypeEnum.SYNC_COMMENT.getCode());
            Map<String, Object> dataJson = BeanUtil.beanToMap(dto);
            dataJson.put("approveSyncFailedType",ApproveSyncFailedTypeEnum.SYNC_COMMENT.getCode());
            approveSyncRecordEntity.setDataJson(dataJson);
            approveSyncRecordFeign.add(approveSyncRecordEntity);
        }
        // 操作日志
        String msg = StrUtil.format("飞书快捷审批端用户【{}】单号为【{}】的单据审核操作  审核结果：【{}】 审核意见 ：【{}】", StringUtils.isNotBlank(dto.getOperatorName()) ? dto.getOperatorName() : dto.getOperator(), dto.getBusinessCode(), ApproveTypeEnum.getName(dto.getApproveType()), dto.getComment());
        Boolean b = operateLogService.addModuleOperateLog(msg, dto.getBusinessKey(), dto.getBusinessId(), "审核操作");
        if(b){
            approveSyncRecordEntity.setStatus(ApproveSyncRecordStatusEnum.SUCCESS.getCode());
            approveSyncRecordFeign.updateById(approveSyncRecordEntity);
        }
        log.info("scm MQSyncProcessCommentService 结束");
    }

}
