package com.common.business.constant;

public class DmpPullConstant {
    private DmpPullConstant() {
    }

    /**
     * dmp_basic_system中code,飞书标识
     */
    public static final String  FS = "feishu";

    /**
     *  dmp_cfg_input中的code,审批定义标识
     */
    public static final String  FS_APPROVALS = "approvals";


    /**
     *  dmp_cfg_input中的code,审批实例id
     */
    public static final String  INSTANCE_IDS = "instanceIds";


    /**
     *  dmp_cfg_input中的code,审批实例详情
     */
    public static final String  INSTANCE = "instance";


    /**
     *  dmp_cfg_input中的code,员工入职事件
     */
    public static final String  USER_CREATED = "contact.user.created_v3";

    /**
     *  dmp_cfg_input中的code,员工离职事件
     */
    public static final String  USER_DELETED = "contact.user.deleted_v3";
}
