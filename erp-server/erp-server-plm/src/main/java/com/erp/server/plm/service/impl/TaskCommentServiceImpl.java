package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskCommentEntity;
import com.erp.model.plm.entity.TaskCommentRefEntity;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.plm.mapper.TaskCommentMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 *
 */
@Service
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskCommentEntity>
        implements TaskCommentService {

    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Resource
    private TaskCommentRefService taskCommentRefService;

    @Resource
    private PlmAttachmentService plmAttachmentService;

    @Resource
    private UserInfoFeign userInfoFeign;


    /**
     * 添加任务评论
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-13 17:58
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveTaskComment(TaskCommentDTO.AddDTO dto) {
        String taskId = dto.getTaskId();
        TaskCommentEntity entity = new TaskCommentEntity();
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        entity.setTaskId(taskId);
        entity.setComment(dto.getComment());
        ProjectTaskEntity taskEntity = projectTaskService.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        Boolean flag = this.save(entity);
        //保存成功 发送评论提醒
        if (flag) {
            List<String> refUserIdList = dto.getRefUserIdList().stream().distinct().collect(Collectors.toList());
            List<FindUserDTO> userList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(refUserIdList)) {
                //添加评论信息
                userList = userInfoFeign.listByUserIds(refUserIdList);
                taskCommentRefService.addCommentRef(refUserIdList, taskId, entity.getId(), userList);
            }
            noticeMessageService.remindRemarkNotice(entity.getId(), loginUser.getUserName(), taskEntity.getProductId(), taskId, dto.getComment(), refUserIdList, userList);
            Class<TaskCommentEntity> customerClass = TaskCommentEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //添加附件
            plmAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), type, entity.getId());

        }
        return flag;
    }


    /**
     * 批量保存
     *
     * @param taskCommentList
     * @return void
     * @author yl
     * @date 2022-10-25 18:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveTaskComment(List<TaskCommentDTO.AddDTO> taskCommentList) {
        if (CollectionUtils.isNotEmpty(taskCommentList)) {
            Class<TaskCommentEntity> customerClass = TaskCommentEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            List<PlmAttachmentEntity> batchAttachmentList = new ArrayList<>(10);

            List<TaskCommentEntity> addList = new ArrayList<>(taskCommentList.size());
            for (TaskCommentDTO.AddDTO item : taskCommentList) {
                TaskCommentEntity addEntity = new TaskCommentEntity();
                String id = IdWorker.getIdStr();
                addEntity.setComment(item.getComment());
                addEntity.setTaskId(item.getTaskId());
                addEntity.setId(id);
                //附件集合
                List<String> attachUrlList = item.getAttachUrlList();
                //附件名
                List<String> attachNameList = item.getAttachNameList();
                if (CollectionUtils.isNotEmpty(attachUrlList) && attachUrlList.size() == attachNameList.size()) {
                    for (int i = 0; i < attachUrlList.size(); i++) {
                        PlmAttachmentEntity attachment = new PlmAttachmentEntity();
                        attachment.setAttachUrl(attachUrlList.get(i));
                        attachment.setAttachName(attachNameList.get(i));
                        attachment.setBusinessId(id);
                        attachment.setType(type);
                        batchAttachmentList.add(attachment);
                    }
                }
                addList.add(addEntity);
            }

            if(CollectionUtils.isNotEmpty(addList)){
                this.saveBatch(addList);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                plmAttachmentService.saveBatch(batchAttachmentList);
            }

        }
    }

    @Override
    public List<TaskCommentDTO.ListDTO> listByTaskId(String taskId) {
        List<TaskCommentEntity> commentList = this.listBaseByTaskId(taskId);
        List<TaskCommentDTO.ListDTO> resultList = new ArrayList<>(commentList.size());
        List<String> commentIdList = commentList.stream().map(TaskCommentEntity::getId).collect(Collectors.toList());
        //任务评论@ 的信息
        List<TaskCommentRefEntity> refList = taskCommentRefService.listByCommentIdList(commentIdList);
        //附件信息
        List<PlmAttachmentEntity> attachList = plmAttachmentService.listByBusinessIds(commentIdList);
        for (TaskCommentEntity item : commentList) {
            TaskCommentDTO.ListDTO info = new TaskCommentDTO.ListDTO();
            String commentId = item.getId();
            info.setComment(item.getComment());
            info.setCommentId(commentId);
            info.setTaskId(item.getTaskId());
            List<String> refUserNameList = refList.stream().filter(r -> r.getTaskCommentId().equals(commentId)).
                    map(TaskCommentRefEntity::getRefUserName).collect(Collectors.toList());
            info.setCommentRefUserNameList(refUserNameList);
            Integer successCount = refList.stream().filter(r -> r.getTaskCommentId().equals(commentId) && r.getSendNoticeResult()).collect(Collectors.toList()).size();
            info.setSendSuccessCount(successCount);
            List<String> attachNameList = attachList.stream().filter(a -> a.getBusinessId().equals(commentId)).
                    map(PlmAttachmentEntity::getAttachName).collect(Collectors.toList());
            List<String> attachUrlList = attachList.stream().filter(a -> a.getBusinessId().equals(commentId)).
                    map(PlmAttachmentEntity::getAttachUrl).collect(Collectors.toList());

            info.setAttachNameList(attachNameList);
            info.setAttachUrlList(attachUrlList);
            info.setCreateUserName(item.getCreateUserName());
            info.setCreateTime(item.getCreateTime());
            resultList.add(info);

        }

        return resultList;
    }

    /**
     * 根据任务id 获取到评论信息
     *
     * @param taskId
     * @return
     */
    private List<TaskCommentEntity> listBaseByTaskId(String taskId) {
        return this.lambdaQuery().eq(TaskCommentEntity::getTaskId, taskId).orderByDesc(TaskCommentEntity::getCreateTime).list();
    }
}




