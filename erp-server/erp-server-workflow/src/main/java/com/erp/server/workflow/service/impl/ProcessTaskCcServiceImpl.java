package com.erp.server.workflow.service.impl;

import com.common.business.dto.FindUserDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.workflow.entity.ProcessTaskCcEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.CcStatusEnum;
import com.erp.server.workflow.mapper.ProcessTaskCcMapper;
import com.erp.server.workflow.service.ProcessTaskCcService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-05-17
 */
@Service
public class ProcessTaskCcServiceImpl extends SuperServiceImpl<ProcessTaskCcMapper, ProcessTaskCcEntity> implements ProcessTaskCcService {
    @Resource
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCcUser(String taskId, List<FindUserDTO> copyUser, String taskManagementId) {
        if(CollectionUtils.isEmpty(copyUser)){
            return;
        }
        for (FindUserDTO user : copyUser) {
            ProcessTaskCcEntity processTaskCcEntity = new ProcessTaskCcEntity();
            processTaskCcEntity.setTaskId(taskId);
            processTaskCcEntity.setCcUserId(user.getUserId());
            processTaskCcEntity.setCcUserName(user.getUserName());
            processTaskCcEntity.setTaskManagementId(taskManagementId);
            this.save(processTaskCcEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCcStatus(String taskId) {
        lambdaUpdate()
                .eq(ProcessTaskCcEntity::getTaskId, taskId)
                .set(ProcessTaskCcEntity::getStatus, CcStatusEnum.SEND)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCcTransfer(List<ProcessTaskManagementEntity> entityList, ProcessTaskManagementEntity insertEntity) {
        if(CollectionUtils.isEmpty(entityList)){
            return;
        }
        ConcurrentHashMap<String, String> ccUserMap = new ConcurrentHashMap<>();
        // 更新抄送人 抄送人的任务id改为新的任务id
        lambdaQuery().in(ProcessTaskCcEntity::getTaskManagementId, entityList.stream().map(ProcessTaskManagementEntity::getId).toArray())
                .list().forEach(processTaskCcEntity -> {
            // 抄送人出现第二次则跳过更新
            if(ccUserMap.containsKey(processTaskCcEntity.getCcUserId())){
                return;
            }else {
                ccUserMap.put(processTaskCcEntity.getCcUserId(), processTaskCcEntity.getCcUserId());
            }
            processTaskCcEntity.setTaskManagementId(insertEntity.getId());
            this.updateById(processTaskCcEntity);
        });
    }

    @Override
    public void sendCcMsg(ProcessTaskManagementEntity entity,String title,String content) {
        List<ProcessTaskCcEntity> list = lambdaQuery()
                .eq(ProcessTaskCcEntity::getTaskId, entity.getTaskId())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> userIdList = list.stream().map(ProcessTaskCcEntity::getCcUserId).distinct().collect(Collectors.toList());
        // 发送抄送消息
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(userIdList);
        noticeMsgInfoDTO.setTitle(title);
        noticeMsgInfoDTO.setContent(content);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.FLW_TASK);
        // 默认tag请指定为msg_notice_default_tag，可以根据不同业务自行指定
        mqProducerService.sendNoticeMsg(noticeMsgInfoDTO, Boolean.TRUE);
        // 审批完成后发送抄送消息更新抄送状态
        updateCcStatus(entity.getTaskId());
    }
}
