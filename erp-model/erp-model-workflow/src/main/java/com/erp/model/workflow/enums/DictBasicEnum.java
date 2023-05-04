package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 基础字典枚举
 *
 * @Author Cloud
 * @Date 2023/4/28 10:11
 **/
public enum DictBasicEnum {

    /**
     * 	approveIng	moduleStatus	待我审核
     * 	waitAuditExecutable	moduleStatus	待我审核-可执行
     * 	assignNotStarted	moduleStatus	待我完成-尚未开始
     * 	waitAuditNotStarted	moduleStatus	待我审核-尚未开始
     * 	assignExecutable	moduleStatus	待我完成-可执行
     * purchase_order	purchaseQty	processCondition	采购数量
     * purchase_order	purchaseAmount	processCondition	订单金额
     * warehouse_receive	receiveQty	processCondition	收货数量
     * 上级选项	fourthLevelSupervisor	superiorOption	直属上级加三级
     * 上级选项	fifthLevelSupervisor	superiorOption	直属上级加四级
     * 上级选项	directSupervisor	superiorOption	直属上级
     * 上级选项	seventhLevelSupervisor	superiorOption	直属上级加六级
     * 上级选项	secondLevelSupervisor	superiorOption	直属上级加一级
     * 上级选项	thirdLevelSupervisor	superiorOption	直属上级加二级
     * 上级选项	sixthLevelSupervisor	superiorOption	直属上级加五级
     * 多人审核处理方式	orSignature	assigneeMulti	或签
     * 多人审核处理方式	jointSignature	assigneeMulti	会签
     * 审批人为空时	escalate	assigneeEmpty	转上级
     * 审批人为空时	rejectApplicant	assigneeEmpty	驳回申请人
     * 审批人选项	somebody	assigneeOption	指定人
     * 审批人选项	role	assigneeOption	角色
     * 审批人选项	superior	assigneeOption	上级
     * 审批人选项	initiator	assigneeOption	发起人
     * 审核设置	adjacentDedupe	reviewSetting	邻节点去重
     * 审核设置	noDedupe	reviewSetting	无去重
     * 审核设置	globalDedupe	reviewSetting	全局去重
     * 超时处理方式	rejectApplicant	timeoutHandling	驳回申请人
     * 超时处理方式	escalate	timeoutHandling	转上级
     */
    APPROVE_ING("approveIng","moduleStatus","待我审核",""),
    WAIT_AUDIT_EXECUTABLE("waitAuditExecutable","moduleStatus","待我审核-可执行",""),
    ASSIGN_NOT_STARTED("assignNotStarted","moduleStatus","待我完成-尚未开始",""),
    WAIT_AUDIT_NOT_STARTED("waitAuditNotStarted","moduleStatus","待我审核-尚未开始",""),
    ASSIGN_EXECUTABLE("assignExecutable","moduleStatus","待我完成-可执行",""),
    PURCHASE_QTY("purchaseQty","processCondition","采购数量","purchase_order"),
    PURCHASE_AMOUNT("purchaseAmount","processCondition","订单金额","purchase_order"),
    RECEIVE_QTY("receiveQty","processCondition","收货数量","warehouse_receive"),
    FOURTH_LEVEL_SUPERVISOR("fourthLevelSupervisor","superiorOption","直属上级加三级","上级选项"),
    FIFTH_LEVEL_SUPERVISOR("fifthLevelSupervisor","superiorOption","直属上级加四级","上级选项"),
    DIRECT_SUPERVISOR("directSupervisor","superiorOption","直属上级","上级选项"),
    SEVENTH_LEVEL_SUPERVISOR("seventhLevelSupervisor","superiorOption","直属上级加六级","上级选项"),
    SECOND_LEVEL_SUPERVISOR("secondLevelSupervisor","superiorOption","直属上级加一级","上级选项"),
    THIRD_LEVEL_SUPERVISOR("thirdLevelSupervisor","superiorOption","直属上级加二级","上级选项"),
    SIXTH_LEVEL_SUPERVISOR("sixthLevelSupervisor","superiorOption","直属上级加五级","上级选项"),
    OR_SIGNATURE("orSignature","assigneeMulti","或签","多人审核处理方式"),
    JOINT_SIGNATURE("jointSignature","assigneeMulti","会签","多人审核处理方式"),
    ESCALATE("escalate","assigneeEmpty","转上级","审批人为空时"),
    REJECT_APPLICANT("rejectApplicant","assigneeEmpty","驳回申请人","审批人为空时"),
    SOMEBODY("somebody","assigneeOption","指定人","审批人选项"),
    ROLE("role","assigneeOption","角色","审批人选项"),
    SUPERIOR("superior","assigneeOption","上级","审批人选项"),
    INITIATOR("initiator","assigneeOption","发起人","审批人选项"),
    ADJACENT_DEDUPE("adjacentDedupe","reviewSetting","邻节点去重","审核设置"),
    NO_DEDUPE("noDedupe","reviewSetting","无去重","审核设置"),
    GLOBAL_DEDUPE("globalDedupe","reviewSetting","全局去重","审核设置"),
    TIMEOUT_HANDLING_REJECT_APPLICANT("rejectApplicant","timeoutHandling","驳回申请人","超时处理方式"),
    TIMEOUT_HANDLING_ESCALATE("escalate","timeoutHandling","转上级","超时处理方式"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String type;
    private String name;
    private String desc;

    DictBasicEnum(String code, String name, String type, String desc) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.desc = desc;
    }


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取DictBasicEnum
     */
    public static DictBasicEnum getByCode(String code) {
        for (DictBasicEnum dictBasicEnum : DictBasicEnum.values()) {
            if (dictBasicEnum.getCode().equals(code)) {
                return dictBasicEnum;
            }
        }
        return null;
    }
}
