package com.common.core.enums;

/**
 * Workflow service error constants.
 */
public final class ApiErrorWorkflow {

    private ApiErrorWorkflow() {
    }

    public static final ApiError WF_PROCESS_NOT_FOUND_OR_ENDED = new ApiError("WF_PROCESS_NOT_FOUND_OR_ENDED", 4000, "流程不存在或者流程已结束");
    public static final ApiError WF_PROCESS_NOT_STARTED = new ApiError("WF_PROCESS_NOT_STARTED", 4001, "流程尚未开始");
    public static final ApiError WF_CURRENT_NODE_NULL = new ApiError("WF_CURRENT_NODE_NULL", 4002, "当前节点为空");
    public static final ApiError WF_PROCESS_ALREADY_STARTED = new ApiError("WF_PROCESS_ALREADY_STARTED", 4003, "流程已启动");
    public static final ApiError WF_PROCESS_ALREADY_ENDED = new ApiError("WF_PROCESS_ALREADY_ENDED", 4004, "流程已结束");
    public static final ApiError WF_PROCESS_INSTANCE_NOT_FOUND = new ApiError("WF_PROCESS_INSTANCE_NOT_FOUND", 4005, "流程实例不存在");
    public static final ApiError WF_PROCESS_MANAGEMENT_NOT_EXIST = new ApiError("WF_PROCESS_MANAGEMENT_NOT_EXIST", 4006, "流程管理不存在");
    public static final ApiError WF_PROCESS_STATUS_NOT_ALLOWED = new ApiError("WF_PROCESS_STATUS_NOT_ALLOWED", 4007, "流程状态为【{0}】不支持审核");
    public static final ApiError WF_RECALL_NOT_FIRST_TASK = new ApiError("WF_RECALL_NOT_FIRST_TASK", 4008, "非首个用户任务节点，无法取回");
    public static final ApiError WF_START_FAILED = new ApiError("WF_START_FAILED", 4009, "启动流程失败");
    public static final ApiError WF_NOT_APPROVER = new ApiError("WF_NOT_APPROVER", 4010, "无权限操作，该任务不属于当前审批人");
    public static final ApiError WF_APPROVE_FAILED = new ApiError("WF_APPROVE_FAILED", 4011, "审核失败");
    public static final ApiError WF_PROCESS_SAVE_FAILED = new ApiError("WF_PROCESS_SAVE_FAILED", 4012, "保存流程定义失败");
    public static final ApiError WF_PROCESS_UPDATE_FAILED = new ApiError("WF_PROCESS_UPDATE_FAILED", 4013, "更新流程定义失败");
    public static final ApiError WF_PROCESS_DEFINITION_NOT_EXIST = new ApiError("WF_PROCESS_DEFINITION_NOT_EXIST", 4014, "流程定义不存在");
    public static final ApiError WF_PROCESS_CANCEL_FAILED = new ApiError("WF_PROCESS_CANCEL_FAILED", 4015, "流程定义撤销失败");
    public static final ApiError WF_PROCESS_ALREADY_DEPLOYED = new ApiError("WF_PROCESS_ALREADY_DEPLOYED", 4016, "流程定义已部署，不需要重复发布");
    public static final ApiError WF_PROCESS_DEPLOY_DELETE_NOT_ALLOWED = new ApiError("WF_PROCESS_DEPLOY_DELETE_NOT_ALLOWED", 4017, "流程定义已部署，不可删除");
    public static final ApiError WF_PROCESS_DEPLOY_UPDATE_NOT_ALLOWED = new ApiError("WF_PROCESS_DEPLOY_UPDATE_NOT_ALLOWED", 4018, "流程定义已发布不支持编辑");
    public static final ApiError WF_PROCESS_CHANGE_NOT_ALLOWED = new ApiError("WF_PROCESS_CHANGE_NOT_ALLOWED", 4019, "流程定义未发布不支持变更");
    public static final ApiError WF_PROCESS_CHANGE_EXIST_NOT_DEPLOY = new ApiError("WF_PROCESS_CHANGE_EXIST_NOT_DEPLOY", 4020, "流程定义已存在未发布数据不支持再次变更");
    public static final ApiError WF_TASK_COMPLETE_FAILED = new ApiError("WF_TASK_COMPLETE_FAILED", 4021, "审核失败，错误信息：{0}");
    public static final ApiError WF_TASK_REJECT_NOT_ALLOWED = new ApiError("WF_TASK_REJECT_NOT_ALLOWED", 4022, "当前任务无法驳回");
    public static final ApiError WF_APPROVE_TASK_INFO_ERROR = new ApiError("WF_APPROVE_TASK_INFO_ERROR", 4023, "查询流程拉取报错,{0}");
    public static final ApiError WF_DEFINITION_NODE_NOT_EXIST = new ApiError("WF_DEFINITION_NODE_NOT_EXIST", 4024, "节点已审核或不存在");
    public static final ApiError WF_TASK_NOT_FOUND = new ApiError("WF_TASK_NOT_FOUND", 4025, "任务已审核或不存在");
    public static final ApiError WF_APPROVE_TASK_NOT_FOUND = new ApiError("WF_APPROVE_TASK_NOT_FOUND", 4026, "流程拉取不存在");
    public static final ApiError WF_TASK_DETAIL_NOT_FOUND = new ApiError("WF_TASK_DETAIL_NOT_FOUND", 4027, "流程拉取明细不存在");
    public static final ApiError WF_MENU_NOT_FOUND = new ApiError("WF_MENU_NOT_FOUND", 4028, "模块编码对应的菜单不存在");
    public static final ApiError WF_MENU_FEIGN_CLASS_NOT_FOUND = new ApiError("WF_MENU_FEIGN_CLASS_NOT_FOUND", 4029, "工作流feign调用的类名不存在,请检查");
    public static final ApiError WF_PROCESS_NOT_START_USER = new ApiError("WF_PROCESS_NOT_START_USER", 4030, "只有流程发起人可执行撤销操作");
    public static final ApiError WF_NEXT_NODE_NO_APPROVER = new ApiError("WF_NEXT_NODE_NO_APPROVER", 4031, "下级节点无审核人，无法提交，请联系管理员");
    public static final ApiError WF_RULE_TYPE_NOT_FOUND = new ApiError("WF_RULE_TYPE_NOT_FOUND", 4032, "未找到流程配置规则类型");
    public static final ApiError WF_RULE_USED_CANNOT_DELETE = new ApiError("WF_RULE_USED_CANNOT_DELETE", 4033, "{0}已被单据使用,不可删除");
    public static final ApiError WF_THIRD_CONFIG_EXIST = new ApiError("WF_THIRD_CONFIG_EXIST", 4034, "单据类型【{0}】下已存在第三方配置，暂不支持再次添加");
    public static final ApiError WF_CREATOR_APPROVER_NOT_SAME = new ApiError("WF_CREATOR_APPROVER_NOT_SAME", 4035, "创建人与审批人不能相同，人员：【{0}】");
    public static final ApiError WF_FIELD_MAP_NOT_FOUND = new ApiError("WF_FIELD_MAP_NOT_FOUND", 4036, "流程字段映射不存在");
    public static final ApiError WF_DELEGATE_CLOSE_ALLOWED_ONLY_RUNNING = new ApiError("WF_DELEGATE_CLOSE_ALLOWED_ONLY_RUNNING", 4037, "仅运行中或待执行状态的流程可终止");
    public static final ApiError WF_DELEGATE_CLOSE_FAILED = new ApiError("WF_DELEGATE_CLOSE_FAILED", 4038, "委托审批单终止失败");
    public static final ApiError WF_DELEGATE_UPDATE_ALLOWED_ONLY_PENDING = new ApiError("WF_DELEGATE_UPDATE_ALLOWED_ONLY_PENDING", 4039, "仅待执行状态的委托审批单可编辑");
    public static final ApiError WF_DELEGATE_TIME_INVALID = new ApiError("WF_DELEGATE_TIME_INVALID", 4040, "委托失效时间不可早于生效时间");
    public static final ApiError WF_DELEGATE_OVERLAP_NOT_ALLOWED = new ApiError("WF_DELEGATE_OVERLAP_NOT_ALLOWED", 4041, "相同发起人、委托流程与时间区间重复，不支持该操作");
    public static final ApiError WF_FS_PROCESS_NOT_EXIST = new ApiError("WF_FS_PROCESS_NOT_EXIST", 4042, "飞书审批流程不存在");
    public static final ApiError WF_FS_QUERY_MULTIPLE_USERS = new ApiError("WF_FS_QUERY_MULTIPLE_USERS", 4043, "查询第三方用户信息返回多条数据，请检查");
    public static final ApiError WF_FS_QUERY_USER_NOT_FOUND = new ApiError("WF_FS_QUERY_USER_NOT_FOUND", 4044, "未找到提审用户的飞书账号，请绑定飞书账号");
    public static final ApiError WF_FS_APPROVE_REQUIRED = new ApiError("WF_FS_APPROVE_REQUIRED", 4045, "当前单据审核流程为飞书流程，请前往飞书审核");
    public static final ApiError WF_MANAGEMENT_FORCE_PASS_NOT_ALLOWED = new ApiError("WF_MANAGEMENT_FORCE_PASS_NOT_ALLOWED", 4046, "强制通过仅适用于运行中或暂停状态的流程");
    public static final ApiError WF_MANAGEMENT_FORCE_REJECT_NOT_ALLOWED = new ApiError("WF_MANAGEMENT_FORCE_REJECT_NOT_ALLOWED", 4047, "强制驳回仅适用于运行中或暂停状态的流程");
    public static final ApiError WF_MANAGEMENT_RESTORE_NOT_ALLOWED = new ApiError("WF_MANAGEMENT_RESTORE_NOT_ALLOWED", 4048, "恢复操作仅适用于暂停状态的流程");
    public static final ApiError WF_MANAGEMENT_SUSPEND_NOT_ALLOWED = new ApiError("WF_MANAGEMENT_SUSPEND_NOT_ALLOWED", 4049, "暂停操作仅适用于运行中状态的流程");
    public static final ApiError WF_RULE_CONFLICT = new ApiError("WF_RULE_CONFLICT", 4050, "{0}流程设置下存在多条符合条件的规则，请检查");
    public static final ApiError WF_MODULE_ALREADY_EXISTS = new ApiError("WF_MODULE_ALREADY_EXISTS", 4051, "模块已存在请不要重复操作");
    public static final ApiError WF_APPROVE_START_FAILED = new ApiError("WF_APPROVE_START_FAILED", 4052, "存在为空的审核人，流程启动失败");
    public static final ApiError WF_NOT_YOUR_APPROVAL = new ApiError("WF_NOT_YOUR_APPROVAL", 4053, "不是您审核的任务，您无法审核");
    public static final ApiError WF_APPROVER_REQUIRED = new ApiError("WF_APPROVER_REQUIRED", 4054, "审核人为空，请先配置任务审核人");
    public static final ApiError WF_REJECT_COMMENT_REQUIRED = new ApiError("WF_REJECT_COMMENT_REQUIRED", 4055, "审核不通过必须填写审核意见");
    public static final ApiError WF_REVOCATION_REQUIRED = new ApiError("WF_REVOCATION_REQUIRED", 4056, "仅在待审核,审核中可申请撤销");
    public static final ApiError WF_APPROVAL_DELETE_FORBIDDEN = new ApiError("WF_APPROVAL_DELETE_FORBIDDEN", 4057, "审核中和审核通过状态不可删除");
    public static final ApiError WF_APPROVE_ALLOWED_STATUS_ONLY = new ApiError("WF_APPROVE_ALLOWED_STATUS_ONLY", 4058, "只有审核中数据支持审核");
    public static final ApiError WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY = new ApiError("WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY", 4059, "只有审核中数据支持撤销流程");
    public static final ApiError WF_FS_PROCESS_USER_NOT_FOUND = new ApiError("WF_FS_PROCESS_USER_NOT_FOUND", 4060, "未找到飞书用户对应的系统用户,飞书userId: {0}");
    public static final ApiError WF_FS_DEFINITION_SUBSCRIBE_FAIL = new ApiError("WF_FS_DEFINITION_SUBSCRIBE_FAIL", 4061, "飞书定义订阅失败，请检查");
    public static final ApiError WF_FS_DEFINITION_UNSUBSCRIBE_FAIL = new ApiError("WF_FS_DEFINITION_UNSUBSCRIBE_FAIL", 4062, "取消飞书定义订阅失败，请检查");
    public static final ApiError WF_APPROVE_TASK_NO_NEED_SYNC_ALLOWED_ONLY_FAIL = new ApiError("WF_APPROVE_TASK_NO_NEED_SYNC_ALLOWED_ONLY_FAIL", 4063, "仅生成失败状态可设置为无需同步");
    public static final ApiError WF_APPROVE_SYNC_RECORD_NO_NEED_SYNC_ALLOWED_ONLY_FAIL = new ApiError("WF_APPROVE_SYNC_RECORD_NO_NEED_SYNC_ALLOWED_ONLY_FAIL", 4064, "仅推送失败状态可设置为无需同步");
    public static final ApiError WF_APPROVE_SYNC_RECORD_NOT_FOUND = new ApiError("WF_APPROVE_SYNC_RECORD_NOT_FOUND", 4065, "三方推送记录不存在");
    public static final ApiError WF_APPROVE_TASK_NO_NEED_SYNC_NOT_ALLOW_OPERATION = new ApiError("WF_APPROVE_TASK_NO_NEED_SYNC_NOT_ALLOW_OPERATION", 4066, "无需同步状态不允许重新生成或状态获取");
    public static final ApiError WF_APPROVE_SYNC_RECORD_NO_NEED_SYNC_NOT_ALLOW_REPUSH = new ApiError("WF_APPROVE_SYNC_RECORD_NO_NEED_SYNC_NOT_ALLOW_REPUSH", 4067, "无需同步状态不允许重推");
    public static final ApiError WF_TASK_RECORD_CONTEXT_REQUIRED = new ApiError("WF_TASK_RECORD_CONTEXT_REQUIRED", 4063, "任务节点上下文为空，禁止直接执行");
    public static final ApiError WF_TASK_RECORD_CONTEXT_MISMATCH = new ApiError("WF_TASK_RECORD_CONTEXT_MISMATCH", 4064, "任务节点上下文不匹配");
    public static final ApiError WF_TASK_RECORD_NOT_MATCH = new ApiError("WF_TASK_RECORD_NOT_MATCH", 4065, "任务节点记录不存在或不匹配");
    public static final ApiError WF_TASK_RECORD_NOT_PROCESSING = new ApiError("WF_TASK_RECORD_NOT_PROCESSING", 4066, "任务节点未处于执行中，禁止直接执行");
    public static final ApiError WF_TASK_RECORD_DUPLICATE = new ApiError("WF_TASK_RECORD_DUPLICATE", 4067, "任务节点已存在，请稍后重试");
    public static final ApiError WF_TASK_RECORD_FORCE_RETRY_PARAM_REQUIRED = new ApiError("WF_TASK_RECORD_FORCE_RETRY_PARAM_REQUIRED", 4068, "强制重试参数不能为空");
    public static final ApiError WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND = new ApiError("WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND", 4069, "未找到可强制重试的任务节点");
    public static final ApiError WF_TASK_RECORD_FORCE_RETRY_FORBIDDEN = new ApiError("WF_TASK_RECORD_FORCE_RETRY_FORBIDDEN", 4070, "无任务节点人工强制重试权限");
    public static final ApiError WF_KOL_B2C_APPROVE_REQUIRED = new ApiError("WF_KOL_B2C_APPROVE_REQUIRED", 4071, "B2C寄样申请单未审核通过，禁止下推");
    public static final ApiError WF_TASK_RECORD_MQ_SEND_FAILED = new ApiError("WF_TASK_RECORD_MQ_SEND_FAILED", 4072, "任务节点MQ发送失败：{0}");
    public static final ApiError WF_TASK_RECORD_FORCE_RETRY_NO_ELIGIBLE = new ApiError("WF_TASK_RECORD_FORCE_RETRY_NO_ELIGIBLE", 4073, "没有可强制重试的任务节点，成功节点不会重试，处理中节点需超过3分钟才允许接管");
    public static final ApiError WF_TASK_RECORD_FORCE_RETRY_PARAM_INCOMPLETE = new ApiError("WF_TASK_RECORD_FORCE_RETRY_PARAM_INCOMPLETE", 4074, "任务节点id或sourceType/sourceId不能为空");
    public static final ApiError WF_TASK_INSTANCE_NOT_FOUND = new ApiError("WF_TASK_INSTANCE_NOT_FOUND", 4079, "任务编排实例不存在");
    public static final ApiError WF_KOL_B2C_SUB_TASK_NODE_NOT_FOUND = new ApiError("WF_KOL_B2C_SUB_TASK_NODE_NOT_FOUND", 4075, "KOL B2C拆分单任务节点配置不存在");
    public static final ApiError WF_KOL_B2C_SPLIT_DETAIL_INCOMPLETE = new ApiError("WF_KOL_B2C_SPLIT_DETAIL_INCOMPLETE", 4076, "B2C寄样申请单拆分单明细不完整，单号【{0}】，达人【{1}】");
    public static final ApiError WF_KOL_B2C_WAIT_SPLIT_ORDER = new ApiError("WF_KOL_B2C_WAIT_SPLIT_ORDER", 4077, "等待拆分单生成");
    public static final ApiError WF_KOL_B2C_WAIT_SUB_TASK_COMPLETE = new ApiError("WF_KOL_B2C_WAIT_SUB_TASK_COMPLETE", 4078, "等待KOL B2C拆分单子任务完成，已完成{0}/{1}");
    public static final ApiError WF_TASK_RECORD_TYPE_NOT_FOUND = new ApiError("WF_TASK_RECORD_TYPE_NOT_FOUND", 4080, "任务节点类型不存在");
    public static final ApiError WF_TASK_INSTANCE_VERSION_CONFLICT = new ApiError("WF_TASK_INSTANCE_VERSION_CONFLICT", 4081, "任务编排实例状态已变化，请刷新后重试");

    static ApiError[] values() {
        return new ApiError[]{
                WF_PROCESS_NOT_FOUND_OR_ENDED,
                WF_PROCESS_NOT_STARTED,
                WF_CURRENT_NODE_NULL,
                WF_PROCESS_ALREADY_STARTED,
                WF_PROCESS_ALREADY_ENDED,
                WF_PROCESS_INSTANCE_NOT_FOUND,
                WF_PROCESS_MANAGEMENT_NOT_EXIST,
                WF_PROCESS_STATUS_NOT_ALLOWED,
                WF_RECALL_NOT_FIRST_TASK,
                WF_START_FAILED,
                WF_NOT_APPROVER,
                WF_APPROVE_FAILED,
                WF_PROCESS_SAVE_FAILED,
                WF_PROCESS_UPDATE_FAILED,
                WF_PROCESS_DEFINITION_NOT_EXIST,
                WF_PROCESS_CANCEL_FAILED,
                WF_PROCESS_ALREADY_DEPLOYED,
                WF_PROCESS_DEPLOY_DELETE_NOT_ALLOWED,
                WF_PROCESS_DEPLOY_UPDATE_NOT_ALLOWED,
                WF_PROCESS_CHANGE_NOT_ALLOWED,
                WF_PROCESS_CHANGE_EXIST_NOT_DEPLOY,
                WF_TASK_COMPLETE_FAILED,
                WF_TASK_REJECT_NOT_ALLOWED,
                WF_APPROVE_TASK_INFO_ERROR,
                WF_DEFINITION_NODE_NOT_EXIST,
                WF_TASK_NOT_FOUND,
                WF_APPROVE_TASK_NOT_FOUND,
                WF_TASK_DETAIL_NOT_FOUND,
                WF_MENU_NOT_FOUND,
                WF_MENU_FEIGN_CLASS_NOT_FOUND,
                WF_PROCESS_NOT_START_USER,
                WF_NEXT_NODE_NO_APPROVER,
                WF_RULE_TYPE_NOT_FOUND,
                WF_RULE_USED_CANNOT_DELETE,
                WF_THIRD_CONFIG_EXIST,
                WF_CREATOR_APPROVER_NOT_SAME,
                WF_FIELD_MAP_NOT_FOUND,
                WF_DELEGATE_CLOSE_ALLOWED_ONLY_RUNNING,
                WF_DELEGATE_CLOSE_FAILED,
                WF_DELEGATE_UPDATE_ALLOWED_ONLY_PENDING,
                WF_DELEGATE_TIME_INVALID,
                WF_DELEGATE_OVERLAP_NOT_ALLOWED,
                WF_FS_PROCESS_NOT_EXIST,
                WF_FS_QUERY_MULTIPLE_USERS,
                WF_FS_QUERY_USER_NOT_FOUND,
                WF_FS_APPROVE_REQUIRED,
                WF_MANAGEMENT_FORCE_PASS_NOT_ALLOWED,
                WF_MANAGEMENT_FORCE_REJECT_NOT_ALLOWED,
                WF_MANAGEMENT_RESTORE_NOT_ALLOWED,
                WF_MANAGEMENT_SUSPEND_NOT_ALLOWED,
                WF_RULE_CONFLICT,
                WF_MODULE_ALREADY_EXISTS,
                WF_APPROVE_START_FAILED,
                WF_NOT_YOUR_APPROVAL,
                WF_APPROVER_REQUIRED,
                WF_REJECT_COMMENT_REQUIRED,
                WF_REVOCATION_REQUIRED,
                WF_APPROVAL_DELETE_FORBIDDEN,
                WF_APPROVE_ALLOWED_STATUS_ONLY,
                WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY,
                WF_FS_PROCESS_USER_NOT_FOUND,
                WF_FS_DEFINITION_SUBSCRIBE_FAIL,
                WF_FS_DEFINITION_UNSUBSCRIBE_FAIL,
                WF_APPROVE_TASK_NO_NEED_SYNC_ALLOWED_ONLY_FAIL,
                WF_APPROVE_SYNC_RECORD_NO_NEED_SYNC_ALLOWED_ONLY_FAIL,
                WF_APPROVE_SYNC_RECORD_NOT_FOUND,
                WF_APPROVE_TASK_NO_NEED_SYNC_NOT_ALLOW_OPERATION,
                WF_APPROVE_SYNC_RECORD_NO_NEED_SYNC_NOT_ALLOW_REPUSH,
                WF_TASK_RECORD_CONTEXT_REQUIRED,
                WF_TASK_RECORD_CONTEXT_MISMATCH,
                WF_TASK_RECORD_NOT_MATCH,
                WF_TASK_RECORD_NOT_PROCESSING,
                WF_TASK_RECORD_DUPLICATE,
                WF_TASK_RECORD_FORCE_RETRY_PARAM_REQUIRED,
                WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND,
                WF_TASK_RECORD_FORCE_RETRY_FORBIDDEN,
                WF_KOL_B2C_APPROVE_REQUIRED,
                WF_TASK_RECORD_MQ_SEND_FAILED,
                WF_TASK_RECORD_FORCE_RETRY_NO_ELIGIBLE,
                WF_TASK_RECORD_FORCE_RETRY_PARAM_INCOMPLETE,
                WF_TASK_INSTANCE_NOT_FOUND,
                WF_KOL_B2C_SUB_TASK_NODE_NOT_FOUND,
                WF_KOL_B2C_SPLIT_DETAIL_INCOMPLETE,
                WF_KOL_B2C_WAIT_SPLIT_ORDER,
                WF_KOL_B2C_WAIT_SUB_TASK_COMPLETE,
                WF_TASK_RECORD_TYPE_NOT_FOUND,
                WF_TASK_INSTANCE_VERSION_CONFLICT,
        };
    }
}
