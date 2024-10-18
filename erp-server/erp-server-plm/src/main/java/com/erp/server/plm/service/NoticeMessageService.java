package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.FlyingBookReminderDTO;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.enums.NoticeEnum;

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

    /**
     * 发布任务通知
     * @param userName
     * @param taskList
     * @param productId
     * @return
     */
    Boolean releaseTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);


    /**
     * 取消发布任务通知
     * @param userName
     * @param taskList
     * @param productId
     * @return
     */
    Boolean cancelReleaseTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);


    /**
     * 开始任务通知
     * @param userName
     * @param taskList
     * @param productId
     * @return
     */
    Boolean startTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);


    /**
     * 完成任务通知
     * @param userName
     * @param taskList
     * @param productId
     * @return
     */
    Boolean finishTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);


    /**
     * 部分完成任务通知
     * @param userName
     * @param taskList
     * @param productId
     * @return
     */
    Boolean portionFinishTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);



    /**
     * 关闭任务通知
     * @param userName
     * @param taskList
     * @param productId
     * @return
     */
    Boolean closeTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);


    /**
     * 审核任务通知
     * @param userName
     * @param taskList
     * @param productId
     * @return
     */
    Boolean approvalTaskNotice(String userName,List<ProjectTaskEntity> taskList,String productId);


    /**
     * 编辑任务通知
     * @param userName
     * @param task
     * @param productId
     * @return
     */
    Boolean editTaskNotice(String userName,ProjectTaskEntity task,String productId);


    /**
     * 删除任务通知
     * @author yl
     * @date 2023-06-21 10:18
     * @param userName
     * @param task
     * @param productId
     * @return java.lang.Boolean
     */
    Boolean deleteTaskNotice(String userName,ProjectTaskEntity task,String productId);


    /**
     * 新建产品发送通知
     * @author yl
     * @date 2023-06-21 10:18
     * @param userName
     * @param productId
     * @return java.lang.Boolean
     */
    Boolean newProductNotice(String userName,String productId);


    /**
     * 产品立项发送通知
     * @author yl
     * @date 2023-06-21 10:18
     * @param userName
     * @param productId
     * @return java.lang.Boolean
     */
    Boolean projectApprovalNotice(String userName,String productId);


    /**
     * 启动项目发送通知
     * @author yl
     * @date 2023-06-21 10:18
     * @param userName
     * @param productId
     * @return java.lang.Boolean
     */
    Boolean startProjectNotice(String userName,String productId);


    /**
     * 开始项目 发送通知
     * @author yl
     * @date 2023-06-21 10:18
     * @param userName
     * @param productId
     * @return java.lang.Boolean
     */
    Boolean beginProjectNotice(String userName,String productId);


    /**
     * 产品审核 发送通知
     */
    Boolean approveProductNotice(String userName, ProductDetailEntity entity);

    /**
     * 完成项目 发送通知
     * @author yl
     * @date 2023-06-21 10:18
     * @param userName
     * @param productId
     * @return java.lang.Boolean
     */
    Boolean finishProjectNotice(String userName,String productId);


    /**
     * 归档项目 发送通知
     * @author yl
     * @date 2023-06-21 10:18
     * @param userName
     * @param productId
     * @return java.lang.Boolean
     */
    Boolean archiveProjectNotice(String userName,String productId);


    /**
     * 任务评论 发送消息
     * @author yl
     * @date 2023-06-21 10:27
     * @param userName
     * @param productId
     * @param taskId
     * @param comment
     * @param refUserIdList
     * @param refUserList
     * @param taskCommentId 任务评论id
     * @return java.lang.Boolean
     */
    Boolean remindRemarkNotice(String taskCommentId ,String userName,String productId,String taskId,String comment,List<String> refUserIdList,List<FindUserDTO> refUserList);

    /**
     * 变更文档 发送通知
     * @author yl
     * @date 2023-06-21 10:20
     * @param userName
     * @param productId
     * @param docName
     * @return
     */
    Boolean docChangesNotice(String userName,String productId,String taskId,String docName);

    /**
     * 警告信息
     */
    void sendEarlyWarning();

    /**
     * 完成待审核的  只有审核人 发送通知
     * @author yl
     * @date 2023-06-21 10:21
     * @param userName
     * @param finishSkuTaskList
     * @param productId
     * @return 
     */
    void finishWaitConfirmNotice(String userName, List<ProjectTaskEntity> finishSkuTaskList, String productId);

    /**
     * 飞书提醒
     * @author yl
     * @date 2023-06-21 10:22
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean flyingBookReminder(FlyingBookReminderDTO dto);

    /**
     * 排期任务提交
     *  @param userName
     *  @param taskList
     *  @param productId
     */
    void scheduleTaskSubmit(String userName, List<ProjectTaskEntity> taskList, String productId);


    /**
     * 排期任务审核通知
     *  @param userName
     *  @param taskList
     *  @param productId
     */
    void scheduleTaskAudit(String userName, List<ProjectTaskEntity> taskList, String productId,String auditResult,  String remark);



    /**
     * 排期任务变动
     *  @param userName
     *  @param taskList
     *  @param productId
     */
    void changeScheduleTask(String userName, List<ProjectTaskEntity> taskList, String productId);


    /**
     * 排期任务 审核人通知
     *  @param userName
     *  @param taskList
     *  @param productId
     */
    void scheduleTaskAuditor(String userName, List<ProjectTaskEntity> taskList, String productId,List<String> auditorList);

    /**
     * 方法说明
     * @author yl
     * @date 2023-06-21 10:23
     * @param flagEnum
     * @return com.erp.model.plm.entity.NoticeMessageEntity
     */
    NoticeMessageEntity getByNodeFlag(NoticeEnum flagEnum);


    /**
     * 通过ID获取通知详情
     */
    NoticeMessageEntity view(String id);

    /**
     * 试产量产审核/试产量产审核完毕 发送通知
     */
    Boolean approvePilotApplicationNotice(String userName, PilotApplicationDTO.ApprovePilotNoticeDTO entity,Boolean isCompeletd);

    List<String> getSetPilotNotice(NoticeMessageEntity notice, PilotApplicationDTO.ApprovePilotNoticeDTO entity, Boolean isCompeletd);
}
