package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 *
 */
public interface NoticeMessageService extends IService<NoticeMessageEntity> {

    PagingVO<List<NoticeMessageDTO>> paging(PagingDTO<BaseSearchDTO> dto);

    Boolean add(NoticeMessageDTO dto);

    Boolean updateNotice(NoticeMessageDTO dto);

    Boolean updateState(UpdateStateDTO dto);

    List<UserNoticeNodeDTO> getUserNoticeNode(String userId);


    Boolean newTaskNotice(List<ProjectTaskEntity> taskList,String productId);

    //发布任务通知
    Boolean releaseTaskNotice(List<ProjectTaskEntity> taskList,String productId);

    //取消发布任务通知
    Boolean cancelReleaseTaskNotice(List<ProjectTaskEntity> taskList,String productId);

    //开始任务通知
    Boolean startTaskNotice(List<ProjectTaskEntity> taskList,String productId);

    //完成任务通知
    Boolean finishTaskNotice(List<ProjectTaskEntity> taskList,String productId);

    //关闭任务通知
    Boolean closeTaskNotice(List<ProjectTaskEntity> taskList,String productId);

    //审核任务通知
    Boolean approvalTaskNotice(List<ProjectTaskEntity> taskList,String productId);

    //编辑任务通知
    Boolean editTaskNotice(ProjectTaskEntity task,String productId);

    //删除任务通知
    Boolean deleteTaskNotice(ProjectTaskEntity task,String productId);

    //新建产品发送通知
    Boolean newProductNotice(String productId);

    //产品立项发送通知
    Boolean projectApprovalNotice(String productId);

    //启动项目 发送通知
    Boolean startProjectNotice(String productId);

    //开始项目 发送通知
    Boolean beginProjectNotice(String productId);

    //完成项目 发送通知
    Boolean finishProjectNotice(String productId);

    //归档项目 发送通知
    Boolean archiveProjectNotice(String productId);


    //评论提醒 发送通知
    Boolean remindRemarkNotice(String productId,String taskId,String comment);

    //变更文档 发送通知
    Boolean docChangesNotice(String productId,String taskId,String docName);

    void sendEarlyWarning();
}
