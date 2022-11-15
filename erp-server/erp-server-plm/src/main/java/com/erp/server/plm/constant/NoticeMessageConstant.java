package com.erp.server.plm.constant;

/**
 * @Classname NoticeMessageConstant
 * @Description TODO
 * @Date 2022-11-15 11:31
 * @Created by yl
 */
public interface NoticeMessageConstant {

    //新建任务
    String NEW_TASK = "状态操作: [%s],新建了一条任务";

    //发布任务
    String RELEASE_TASK = "状态操作: [%s],分配了1条任务给你，请及时查看";

    //取消发布
    String CANCEL_RELEASE = "状态操作: [%s],将状态由【已发布】变更为【待发布】";

    //开始任务
    String START_TASK = "状态操作: [%s], 将状态由【未开始】变更为【进行中】";

    //完成任务
    String FINISH_TASK = "状态操作: [%s] 将状态由【进行中】变更为【已完成】";

    //关闭任务
    String CLOSE_TASK = "状态操作: [%s] 将状态由【进行中】变更为【已关闭】";

    //审核任务
    String APPROVAL_TASK = "状态操作: [%s] 将状态由【待审核】变更为【%s】";

    //变更文档
    String DOC_CHANGES = "更新通知: [%s] 操作文档更新为【%s】";

    //评论提醒
    String REMIND_REMARK = "评论通知: [%s]  操作新增一条评论【%s】";


    //编辑任务
    String EDIT_TASK = "任务通知: [%s] 编辑了任务【%s】";

    //删除任务
    String DELETE_TASK = "任务通知: [%s] 删除任务【%s】";

    //预警提醒
    String EARLY_WARNING = "预警提醒: %s";

    //新建产品
    String NEW_PRODUCT = "产品提醒: %s 新建产品名称【%s】";

    //产品立项
    String PROJECT_APPROVAL = "产品提醒: %s 操作【%s】为已立项";

    //启动项目
    String START_PROJECT = "项目状态: %s 启动【%s】,将状态由【未启动】变更为【已启动】";

    //开始项目
    String BEGIN_PROJECT = "项目状态: %s 操作 【%s】,将状态由【已启动】变更为【进行中】";

    //完成项目
    String FINISH_PROJECT = "项目状态: %s 操作 【%s】,将状态由【进行中】变更为【已完成】";

    //归档项目
    String ARCHIVE_PROJECT = "项目状态: %s 已归档【%s】";











}
