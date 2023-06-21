package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskCommentEntity;
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


/**
 *
 */
@Service
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskCommentEntity>
        implements TaskCommentService {

    @Autowired
    private CommonService commonService;

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
        LoginUser loginUser = commonService.getUserInfo();
        entity.setTaskId(taskId);
        entity.setComment(dto.getComment());
        ProjectTaskEntity taskEntity = projectTaskService.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        Boolean flag = this.save(entity);
        //保存成功 发送评论提醒
        if (flag) {
            List<String> refUserIdList = dto.getRefUserIdList();
            List<FindUserDTO> userList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(refUserIdList)){
                //添加评论信息
                userList = userInfoFeign.listByUserIds(refUserIdList);
                taskCommentRefService.addCommentRef(refUserIdList, taskId, entity.getId(),userList);
            }
            noticeMessageService.remindRemarkNotice(entity.getId(),loginUser.getUserName(), taskEntity.getProductId(), taskId, dto.getComment(), dto.getRefUserIdList(),userList);
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
    public void batchSaveTaskComment(List<TaskCommentEntity> taskCommentList) {
        if (CollectionUtils.isNotEmpty(taskCommentList)) {
            this.saveBatch(taskCommentList);
        }
    }

    @Override
    public List<TaskCommentDTO.ListDTO> listByTaskId(String taskId) {
        return null;
    }
}




