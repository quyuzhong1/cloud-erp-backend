package com.erp.server.workflow.constant;

/**
 * 飞书事件常量
 * @author will
 * @date 2025/12/15 11:28
 */
public interface FsEventConstant {

    /**
     * 当审批实例状态变更：有审批实例创建、同意、拒绝、撤回时推送事件
     */
      String APPROVAL_INSTANCE_EVENT = "approval_instance";
    /**
     * 审批任务状态变更：当有审批任务创建、同意、拒绝、转交、完成时推送事件
     */
      String APPROVAL_TASK_EVENT = "approval_task";
    /**
     * 审批通过：当审批被通过时推送事件
     */
      String APPROVAL_EVENT = "approval";
    /**
     * 加班审批：加班审批通过后推送消息
     */
      String WORK_APPROVAL_EVENT = "work_approval";
    /**
     * 请假审批：请假审批通过后推送消息
     */
      String LEAVE_APPROVAL_EVENT = "leave_approval";
    /**
     * 换班审批：换班审批通过后推送消息
     */
      String SHIFT_APPROVAL_EVENT = "shift_approval";
    /**
     * 审批抄送事件
     */
      String APPROVAL_CC_EVENT = "approval_cc";
    /**
     * 出差审批：出差审批通过后推送消息
     */
      String TRIP_APPROVAL_EVENT = "trip_approval";
    /**
     * 外出审批事件
     */
      String OUT_APPROVAL_EVENT = "out_approval";
    /**
     * 补卡审批：补卡审批通过后推送消息
     */
      String REMEDY_APPROVAL_EVENT = "remedy_approval";
    /**
     * 员工入职事件订阅
     */
    String USER_CREATED_EVENT = "contact.user.created_v3";
    /**
     * 员工离职事件订阅
     */
    String USER_DELETED_EVENT = "contact.user.deleted_v3";

}
