package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.TimeoutStatusEnum;
import com.erp.server.workflow.mapper.ProcessTaskManagementMapper;
import com.erp.server.workflow.service.ProcessTaskCcService;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import com.common.business.service.SuperServiceImpl;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class ProcessTaskManagementServiceImpl extends SuperServiceImpl<ProcessTaskManagementMapper, ProcessTaskManagementEntity> implements ProcessTaskManagementService {

    @Resource
    private ProcessTaskCcService processTaskCcService;

    @Override
    public Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String comment, String activityId) {
        ProcessTaskManagementEntity entity = getById(taskId);
        boolean update;
        if(ApproveTypeEnum.REJECT_APPOINT.equals(approveType)) {
            // 驳回到指定节点
            update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.REJECT)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(StrUtil.isNotBlank(comment), ProcessTaskManagementEntity::getRemark, comment)
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurApproveId())
                    .set(ProcessTaskManagementEntity::getApproveName, entity.getCurApproveName())
                    .eq(ProcessTaskManagementEntity::getProcessInstanceId, entity.getProcessInstanceId())
                    .ne(ProcessTaskManagementEntity::getCurActivityId, activityId)
                    .update();
        }else {
            // 更新任务审批状态
            update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveTypeEnum.PASS.equals(approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(StrUtil.isNotBlank(comment), ProcessTaskManagementEntity::getRemark, comment)
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurApproveId())
                    .set(ProcessTaskManagementEntity::getApproveName, entity.getCurApproveName())
                    .eq(ProcessTaskManagementEntity::getExecutionId, entity.getExecutionId())
                    .eq(ProcessTaskManagementEntity::getTaskId, entity.getTaskId())
                    .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE_ING)
                    .eq(ProcessTaskManagementEntity::getCurActivityId, entity.getCurActivityId())
                    .update();
        }
        if (!update) {
            throw new RuntimeException("更新任务审批状态失败");
        }
        // 发送抄送消息

//        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
//        noticeMsgInfoDTO.setReceiverUserIds(new ArrayList<>(Arrays.asList("1645710077245652993")));
//        noticeMsgInfoDTO.setTitle("产品提醒: 张三 新建产品名称【iphone14】");
//        // 请注意：飞书中的**和**中间的数据表示加粗
//        noticeMsgInfoDTO.setContent("**产品名称: **iphone14\n**产品日期：**2023-04-20");
//        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_TASK);
//        // 默认tag请指定为msg_notice_default_tag，可以根据不同业务自行指定
//        SendResult sendResult = mqProducerService.sendNoticeMsg(noticeMsgInfoDTO, null);
//        // 审批完成后发送抄送消息更新抄送状态
//        processTaskCcService.updateCcStatus(entity.getProcessInstanceId(), entity.getTaskId());
        return Boolean.TRUE;
    }

    @Override
    public LinkedHashMap<String, List<ProcessTaskManagementEntity>> listHisByProcessInstanceId(String processInstanceId, Integer num) {
        // 查询所有历史任务
        List<ProcessTaskManagementEntity> list = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceId)
                .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE)
                .orderByDesc(ProcessTaskManagementEntity::getCreateTime)
                .list();
        if(CollectionUtil.isEmpty(list)){
            return new LinkedHashMap<>();
        }
        LinkedHashMap<String, List<ProcessTaskManagementEntity>> nodeMap = list.stream()
                .collect(Collectors.groupingBy(ProcessTaskManagementEntity::getCurActivityId, LinkedHashMap::new, Collectors.toList()));
        return nodeMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProcessTask(ProcessTaskManagementEntity insertTask) {
        // 赋值上级节点id
        ProcessTaskManagementEntity entity = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, insertTask.getProcessInstanceId())
                .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE)
                .ne(ProcessTaskManagementEntity::getCurActivityId, insertTask.getCurActivityId())
                .orderByDesc(ProcessTaskManagementEntity::getCreateTime)
                .last("limit 1")
                .one();
        if(null != entity){
            insertTask.setPreActivityId(entity.getCurActivityId());
        }
        boolean save = save(insertTask);
        if (!save) {
            throw new RuntimeException("保存流程任务失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransfer(String taskId, String targetUserId, String targetUserName, String sourceUserId, String remark) {
        List<ProcessTaskManagementEntity> entityList = lambdaQuery()
                .eq(ProcessTaskManagementEntity::getTaskId, taskId)
                .eq(StrUtil.isNotBlank(sourceUserId), ProcessTaskManagementEntity::getCurApproveId, sourceUserId)
                .eq(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE_ING)
                .list();
        if(CollectionUtil.isEmpty(entityList)){
            throw new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS);
        }
        // 关闭原有记录
        for (ProcessTaskManagementEntity entity : entityList) {
            boolean update = lambdaUpdate()
                    .set(ProcessTaskManagementEntity::getTaskStatus, ApproveStatusEnum.APPROVE)
                    .set(ProcessTaskManagementEntity::getApproveTime, LocalDateTime.now())
                    .set(ProcessTaskManagementEntity::getApproveId, entity.getCurApproveId())
                    .set(ProcessTaskManagementEntity::getRemark, StrUtil.format("【{}】已将任务转移给【{}】办理，备注：{}", entity.getCurApproveName(), targetUserName, remark))
                    .eq(ProcessTaskManagementEntity::getId, entity.getId())
                    .update();
        }
        // 新增审批记录
        boolean save = save(ProcessTaskManagementEntity.getByEntity(entityList.get(0), targetUserId));
        if (!save) {
            throw new RuntimeException(" updateTransfer 任务转办 保存流程任务失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByProcessInstanceId(String processInstanceId) {
        boolean remove = lambdaUpdate()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceId)
                .remove();
        if (!remove) {
            throw new RuntimeException("删除流程任务失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTimeoutStatus(List<String> taskManagementIds, TimeoutStatusEnum timeoutStatusEnum) {
        boolean update = lambdaUpdate()
                .set(ProcessTaskManagementEntity::getTimeoutStatus, timeoutStatusEnum)
                .in(ProcessTaskManagementEntity::getId, taskManagementIds)
                .update();
        if (!update) {
            throw new RuntimeException("更新任务超时状态失败");
        }
    }
}
