package com.erp.server.plm.constant;

/**
 * @Classname NoticeMessageConstant

 * @Date 2022-11-15 11:31
 * @Created by yl
 */
public interface NoticeMessageConstant {

    /**
     * 新建任务
     */
    String NEW_TASK = "状态操作: [%s],新建了一条任务";

    /**
     * 分配任务
     */
    String RELEASE_TASK = "状态操作: [%s],分配了%s条任务给你，请及时查看";


    /**
     * 发布任务
     */
    String RELEASE_TASK_OTHER = "状态操作: [%s]发布了%s条任务";

    /**
     * 取消发布
     */
    String CANCEL_RELEASE = "状态操作: [%s],将状态由【已发布】变更为【待发布】";


    /**
     * 开始任务
     */
    String START_TASK = "状态操作: [%s], 将状态由【未开始】变更为【进行中】";

    /**
     * 完成任务
     */
    String FINISH_TASK = "状态操作: [%s] 将状态由【进行中】变更为【已完成】";

    /**
     * 前置任务完成任务
     */
    String EXIST_PRE_FINISH_TASK = "前置任务-[%s]已完成，可以开始执行[%s]";

    /**
     *  完成待审核
     */
    String FINISH_WAIT_CONFIRM = "【%s】有一条任务待您审核，请及时审核";

    String FINISH_WAIT_CONFIRM_PRESS = "【%s催办】您有一条任务待审核，请及时审核";
    String TASK_CHARGE_PRESS = "【%s催办】您有一条任务待处理，请及时处理";

    /**
     *   部分完成任务
     */
    String  PORTION_FINISH_TASK = "状态操作: [%s] 将状态由【进行中】变更为【已完成】";

    /**
     *  取消任务
     */
    String CLOSE_TASK = "状态操作: [%s] 将状态由【进行中】变更为【已取消】";


    /**
     *  审核任务
     */
    String APPROVAL_TASK = "状态操作: [%s] 将状态由【待审核】变更为【%s】";

    /**
     * 变更文档
     */
    String DOC_CHANGES = "更新通知: [%s] 操作文档更新为【%s】";

    /**
     *  评论提醒
     */
    String REMIND_REMARK = "评论通知: [%s]  操作新增一条评论【%s】";

    /**
     *  评论@提醒
     */
    String REMIND_REMARK_REF = "[%s]:@[%s]  【%s】";


    /**
     * 编辑任务
     */
    String EDIT_TASK = "任务通知: [%s] 编辑了任务【%s】";

    /**
     *   删除任务
     */
    String DELETE_TASK = "任务通知: [%s] 删除任务【%s】";


    /**
     * 预警提醒
     */
    String EARLY_WARNING = "预警提醒: %s";


    /**
     * 新建产品
     */
    String NEW_PRODUCT = "产品提醒: %s 新建产品名称【%s】";


    /**
     * 产品立项
     */
    String PROJECT_APPROVAL = "产品提醒: %s 操作【%s】为已立项";


    /**
     * 启动项目
     */
    String START_PROJECT = "项目状态: %s 启动【%s】,将状态由【未启动】变更为【已启动】";


    /**
     * 开始项目
     */
    String BEGIN_PROJECT = "项目状态: %s 操作 【%s】,将状态由【已启动】变更为【进行中】";


    /**
     * 完成项目
     */
    String FINISH_PROJECT = "项目状态: %s 操作 【%s】,将状态由【进行中】变更为【已完成】";

    /**
     * 审核产品
     */
    String APPROVE_PRODUCT = "SKU【%s】产品资料审核通过";

    /**
     * 归档项目
     */
    String ARCHIVE_PROJECT = "项目状态: %s 已归档【%s】";



    /**
     * 项目信息
     */
    String PROJECT_CONTENT= "**产品名称: **%s\n**%s：**%s";


    String TASK_PROJECT_CONTENT= "**任务名称：**%s\n**产品名称：**%s\n**截止日期：**%s\n**%s：**%s";

    /**
     *  排期任务提交
     */
    String SCHEDULE_TASK_CONTENT= "【%s】提交%S条排期审核,请及时查看";

    /**
     *  排期任务通过
     */
    String SCHEDULE_TASK_PASS_CONTENT= "【%s】排期审核结果：通过，任务已自动发布给任务负责人";

    /**
     *  排期任务不通过
     */
    String SCHEDULE_TASK_NO_PASS_CONTENT= "【%s】排期审核结果：不通过【原因：%s】";


    /**
     *  排期任务变更
     */
    String SCHEDULE_TASK_CHANGE_CONTENT= "你的%s个任务排期发生变动，请及时查看任务列表";

    /**
     *  任务排期 提交 卡片
     */
    String SCHEDULE_TASK_SUBMIT_CARD="**任务名称：**%s\n**产品名称：**%s\n**产品经理：**%s";


    /**
     *  任务排期 审核 卡片
     */
    String SCHEDULE_TASK_AUDIT_CARD="**任务数量：**%s\n**产品名称：**%s\n**产品经理：**%s";


    /**
     *  任务排期 变更 卡片
     */
    String SCHEDULE_TASK_CHANGE_CARD="**任务名称：**%s\n**产品名称：**%s\n**任务负责人：**%s";

    /**
     * 试产量产审核：通知内容
     */
    String AUDIT_PILOT_MSG_CONTENT = "**通知类型：**%s\n**产品经理：**%s\n**SKU：**%s";















}
