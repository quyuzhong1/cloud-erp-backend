package com.erp.server.plm.enums;

/**
 * @Classname NoticeEnum
 * @Description TODO
 * @Date 2022-11-11 10:57
 * @Created by yl
 */
public enum NoticeEnum {
    NEW_TASK("newTask", "新建任务"),
    RELEASE_TASK("releaseTask", "发布任务"),
    START_TASK("startTask", "开始任务"),
    FINISH_TASK("finishTask", "完成任务"),
    CLOSE_TASK("closeTask", "关闭任务"),
    APPROVAL_TASK("approvalTask", "审核任务"),
    DELETE_TASK("deleteTask", "删除任务"),
    EDIT_TASK("editTask", "编辑任务"),
    CANCEL_RELEASE("cancelRelease","取消发布"),
    DOC_CHANGES("docChanges", "文档变更"),
    REMIND_REMARK("remindRemark", "评论提醒"),
    EARLY_WARNING("earlyWarning","预警提醒"),
    NEW_PRODUCT("newProduct","新建产品"),
    PROJECT_APPROVAL("projectApproval","产品立项"),
    START_PROJECT ("startProject","启动项目"),
    BEGIN_PROJECT("beginProject","开始项目"),
    FINISH_PROJECT("finishProject","完成项目"),
    ARCHIVE_PROJECT("archiveProject","归档项目");





    private String flag;
    private String name;

    NoticeEnum(String flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }


    public static String getName(String flag) {
        for (NoticeItemPeopleEnum item : NoticeItemPeopleEnum.values()) {
            if (flag.equals(item.getFlag())) {
                return item.getName();
            }
        }
        return "";
    }
}
