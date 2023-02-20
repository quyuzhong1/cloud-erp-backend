package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.dto.base.UpdateStateDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.FlyingBookReminderDTO;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;

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


    Boolean newTaskNotice(String userName, List<ProjectTaskEntity> taskList,String productId);

    //发布任务通知
    Boolean releaseTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);

    //取消发布任务通知
    Boolean cancelReleaseTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);

    //开始任务通知
    Boolean startTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);

    //完成任务通知
    Boolean finishTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);

    //部分完成任务通知
    Boolean portionFinishTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);


    //关闭任务通知
    Boolean closeTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);

    //审核任务通知
    Boolean approvalTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);

    //编辑任务通知
    Boolean editTaskNotice(String userName,ProjectTaskEntity task,String productId);

    //删除任务通知
    Boolean deleteTaskNotice(String userName,ProjectTaskEntity task,String productId);

    //新建产品发送通知
    Boolean newProductNotice(String userName,String productId);

    //产品立项发送通知
    Boolean projectApprovalNotice(String userName,String productId);

    //启动项目 发送通知
    Boolean startProjectNotice(String userName,String productId);

    //开始项目 发送通知
    Boolean beginProjectNotice(String userName,String productId);

    //完成项目 发送通知
    Boolean finishProjectNotice(String userName,String productId);

    //归档项目 发送通知
    Boolean archiveProjectNotice(String userName,String productId);


    //评论提醒 发送通知
    Boolean remindRemarkNotice(String userName,String productId,String taskId,String comment);

    //变更文档 发送通知
    Boolean docChangesNotice(String userName,String productId,String taskId,String docName);

    void sendEarlyWarning();

    //完成待审核的  只有审核人 发送通知
    void finishWaitConfirmNotice(String userName, List<ProjectTaskEntity> finishSkuTaskList, String productId);


    Boolean flyingBookReminder(FlyingBookReminderDTO dto);

    /**
     * 排期任务提交
     */
    void scheduleTaskSubmit(String userName, List<ProjectTaskEntity> taskList, String productId);

    /**
     * 排期任务审核通知
     */
    void scheduleTaskAudit(String userName, List<ProjectTaskEntity> taskList, String productId,String auditResult,  String remark);


    /**
     * 排期任务变动
     */
    void changeScheduleTask(String userName, List<ProjectTaskEntity> taskList, String productId);
}
