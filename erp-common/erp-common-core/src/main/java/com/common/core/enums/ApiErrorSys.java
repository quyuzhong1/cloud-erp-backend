package com.common.core.enums;

/**
 * Sys service error constants.
 */
public final class ApiErrorSys {

    private ApiErrorSys() {
    }


    public static final ApiError AUTH_PASSWORD_MISMATCH = new ApiError("AUTH_PASSWORD_MISMATCH", 2000, "两次密码不一致");
    public static final ApiError AUTH_LOGIN_FAILED = new ApiError("AUTH_LOGIN_FAILED", 2001, "登录失败!");
    public static final ApiError AUTH_ACCOUNT_DISABLED = new ApiError("AUTH_ACCOUNT_DISABLED", 2002, "账户【{0}】已禁用!");
    public static final ApiError AUTH_USER_NOT_FOUND = new ApiError("AUTH_USER_NOT_FOUND", 2003, "用户【{0}】不存在");
    public static final ApiError AUTH_LOGIN_LOCKED = new ApiError("AUTH_LOGIN_LOCKED", 2004, "账户密码已输入错误5次，请在1小时后重试");
    public static final ApiError AUTH_LOGIN_RETRY_LEFT = new ApiError("AUTH_LOGIN_RETRY_LEFT", 2005, "账号密码错误,还可尝试【{0}】次,失败后将锁定1小时");
    public static final ApiError AUTH_CREDENTIALS_INVALID = new ApiError("AUTH_CREDENTIALS_INVALID", 2006, "用户不存在或者密码错误");
    public static final ApiError AUTH_PASSWORD_REQUIRED = new ApiError("AUTH_PASSWORD_REQUIRED", 2007, "密码不能为空");
    public static final ApiError AUTH_ACCOUNT_PASSWORD = new ApiError("AUTH_ACCOUNT_PASSWORD", 2008, "账号密码错误");
    public static final ApiError AUTH_ACCOUNT_BIND_FAILED = new ApiError("AUTH_ACCOUNT_BIND_FAILED", 2009, "绑定账号失败");
    public static final ApiError AUTH_ACCOUNT_NOT_BOUND = new ApiError("AUTH_ACCOUNT_NOT_BOUND", 2010, "账号尚未绑定请绑定后在登录");
    public static final ApiError AUTH_ACCOUNT_ALREADY_BOUND = new ApiError("AUTH_ACCOUNT_ALREADY_BOUND", 2011, "该账号已经绑定");
    public static final ApiError AUTH_USERNAME_EXISTS = new ApiError("AUTH_USERNAME_EXISTS", 2012, "用户名称已存在");
    public static final ApiError AUTH_ACCOUNT_NOT_FOUND = new ApiError("AUTH_ACCOUNT_NOT_FOUND", 2013, "账号不存在");
    public static final ApiError AUTH_MOBILE_IS_EXIST = new ApiError("AUTH_MOBILE_IS_EXIST", 2014, "手机号已注册");
    public static final ApiError AUTH_FS_USER_NOT_BIND = new ApiError("AUTH_FS_USER_NOT_BIND", 2015, "当前用户未绑定飞书账号");
    public static final ApiError AUTH_FS_NOT_BOUND = new ApiError("AUTH_FS_NOT_BOUND", 2016, "尚未绑定飞书，请在ERP系统[个人中心]绑定飞书后可查看通知");
    public static final ApiError AUTH_MODIFY_DELETE_DENIED = new ApiError("AUTH_MODIFY_DELETE_DENIED", 2017, "您无权修改或删除");
    public static final ApiError AUTH_VIEW_DENIED = new ApiError("AUTH_VIEW_DENIED", 2018, "您没有权限查看");
    public static final ApiError AUTH_SSO_APP_NOT_FOUND = new ApiError("AUTH_SSO_APP_NOT_FOUND", 2019, "应用不存在");
    public static final ApiError AUTH_SSO_DISABLED = new ApiError("AUTH_SSO_DISABLED", 2020, "单点登录功能已禁用");
    public static final ApiError AUTH_SSO_DECRYPT_FAILED = new ApiError("AUTH_SSO_DECRYPT_FAILED", 2021, "单点登录信息解密失败");
    public static final ApiError AUTH_SSO_PAYLOAD_PARSE_FAILED = new ApiError("AUTH_SSO_PAYLOAD_PARSE_FAILED", 2022, "单点登录Payload解析失败");
    public static final ApiError AUTH_SSO_INVALID_PAYLOAD = new ApiError("AUTH_SSO_INVALID_PAYLOAD", 2023, "单点登录Payload内容无效");
    public static final ApiError AUTH_SSO_USER_NOT_BOUND_ERP = new ApiError("AUTH_SSO_USER_NOT_BOUND_ERP", 2024, "用户未绑定ERP系统");
    public static final ApiError AUTH_SSO_SYSTEM_ERROR = new ApiError("AUTH_SSO_SYSTEM_ERROR", 2025, "单点登录系统异常：{0}");
    public static final ApiError AUTH_ARCHIVE_DENIED = new ApiError("AUTH_ARCHIVE_DENIED", 2026, "归档系统不允许增删改数据");
    public static final ApiError AUTH_MENU_FETCH_FAILED = new ApiError("AUTH_MENU_FETCH_FAILED", 2027, "获取菜单权限失败，请稍后重试");
    public static final ApiError AUTH_PERMISSION_FETCH_FAILED = new ApiError("AUTH_PERMISSION_FETCH_FAILED", 2028, "获取按钮权限失败，请稍后重试");
    public static final ApiError AUTH_API_TOKEN_EXPIRED_RECREATE = new ApiError("AUTH_API_TOKEN_EXPIRED_RECREATE", 2027, "个人访问令牌已过期，请重新生成");
    public static final ApiError AUTH_API_TOKEN_ID_REQUIRED = new ApiError("AUTH_API_TOKEN_ID_REQUIRED", 2028, "id不能为空");
    public static final ApiError AUTH_API_TOKEN_NOT_FOUND = new ApiError("AUTH_API_TOKEN_NOT_FOUND", 2029, "个人访问令牌不存在");
    public static final ApiError AUTH_API_TOKEN_USER_NOT_LOGIN = new ApiError("AUTH_API_TOKEN_USER_NOT_LOGIN", 2030, "用户未登录");
    public static final ApiError AUTH_API_TOKEN_NAME_REQUIRED = new ApiError("AUTH_API_TOKEN_NAME_REQUIRED", 2031, "令牌名称不能为空");
    public static final ApiError AUTH_API_TOKEN_NAME_TOO_LONG = new ApiError("AUTH_API_TOKEN_NAME_TOO_LONG", 2032, "令牌名称最大长度不能超过50位");
    public static final ApiError AUTH_API_TOKEN_VALIDITY_REQUIRED = new ApiError("AUTH_API_TOKEN_VALIDITY_REQUIRED", 2033, "有效期不能为空");
    public static final ApiError AUTH_API_TOKEN_VALIDITY_INVALID = new ApiError("AUTH_API_TOKEN_VALIDITY_INVALID", 2034, "有效期只能选择30天、90天、180天、365天或永不过期");
    public static final ApiError AUTH_API_TOKEN_ENCRYPT_FAILED = new ApiError("AUTH_API_TOKEN_ENCRYPT_FAILED", 2035, "加密令牌失败");
    public static final ApiError AUTH_API_TOKEN_CIPHERTEXT_INVALID = new ApiError("AUTH_API_TOKEN_CIPHERTEXT_INVALID", 2036, "令牌密文格式不合法");
    public static final ApiError AUTH_API_TOKEN_DECRYPT_FAILED = new ApiError("AUTH_API_TOKEN_DECRYPT_FAILED", 2037, "解密令牌失败，请确认API Token加密密钥配置未变更");
    public static final ApiError AUTH_API_TOKEN_AES_KEY_REQUIRED = new ApiError("AUTH_API_TOKEN_AES_KEY_REQUIRED", 2038, "API Token加密密钥未配置，请配置erp.api-token.aes-key或环境变量ERP_API_TOKEN_AES_KEY");
    public static final ApiError AUTH_API_TOKEN_AES_KEY_INIT_FAILED = new ApiError("AUTH_API_TOKEN_AES_KEY_INIT_FAILED", 2039, "初始化API Token加密密钥失败");
    public static final ApiError AUTH_API_TOKEN_HASH_FAILED = new ApiError("AUTH_API_TOKEN_HASH_FAILED", 2040, "生成令牌哈希失败");
    public static final ApiError AUTH_API_TOKEN_PATH_REQUIRED = new ApiError("AUTH_API_TOKEN_PATH_REQUIRED", 2041, "接口路径不能为空");
    public static final ApiError AUTH_API_TOKEN_PATH_BACKSLASH_FORBIDDEN = new ApiError("AUTH_API_TOKEN_PATH_BACKSLASH_FORBIDDEN", 2042, "接口路径不允许包含反斜杠");
    public static final ApiError AUTH_API_TOKEN_PATH_INVALID = new ApiError("AUTH_API_TOKEN_PATH_INVALID", 2043, "接口路径格式不合法");
    public static final ApiError AUTH_API_TOKEN_PATH_TRAVERSAL_FORBIDDEN = new ApiError("AUTH_API_TOKEN_PATH_TRAVERSAL_FORBIDDEN", 2044, "接口路径不允许包含路径穿越");
    public static final ApiError AUTH_API_TOKEN_PATH_TOO_LONG = new ApiError("AUTH_API_TOKEN_PATH_TOO_LONG", 2045, "接口路径最大长度不能超过500位");
    public static final ApiError AUTH_API_TOKEN_WHITELIST_NOT_FOUND = new ApiError("AUTH_API_TOKEN_WHITELIST_NOT_FOUND", 2046, "接口白名单配置不存在");
    public static final ApiError AUTH_API_TOKEN_PATH_EXISTS = new ApiError("AUTH_API_TOKEN_PATH_EXISTS", 2047, "接口路径已存在");
    public static final ApiError AUTH_API_TOKEN_MANAGEMENT_PATH_FORBIDDEN = new ApiError("AUTH_API_TOKEN_MANAGEMENT_PATH_FORBIDDEN", 2048, "API Token不允许访问管理接口");
    public static final ApiError AUTH_API_TOKEN_PATH_NOT_IN_WHITELIST = new ApiError("AUTH_API_TOKEN_PATH_NOT_IN_WHITELIST", 2049, "接口未配置API Token白名单");
    public static final ApiError AUTH_API_TOKEN_WHITELIST_ADMIN_REQUIRED = new ApiError("AUTH_API_TOKEN_WHITELIST_ADMIN_REQUIRED", 2050, "仅管理员可维护API Token接口白名单");
    public static final ApiError QUERY_NOT_EXTEND_METHOD = new ApiError("QUERY_NOT_EXTEND_METHOD", 700, "扩展字段没有配置查询脚本");
    public static final ApiError QUERY_ILLEGAL_FIELD = new ApiError("QUERY_ILLEGAL_FIELD", 701, "非法的查询字段或查询值");
    public static final ApiError QUERY_ILLEGAL_COND = new ApiError("QUERY_ILLEGAL_COND", 702, "非法的查询连接条件");
    public static final ApiError QUERY_LIST_TYPE_ERROR = new ApiError("QUERY_LIST_TYPE_ERROR", 703, "在...列表或不在...列表查询应传递数组");
    public static final ApiError QUERY_BETWEEN_ERROR = new ApiError("QUERY_BETWEEN_ERROR", 704, "介于条件需要填起始时间和开始时间");
    public static final ApiError QUERY_ILLEGAL_DATE_FORMAT = new ApiError("QUERY_ILLEGAL_DATE_FORMAT", 705, "非法日期格式");
    public static final ApiError QUERY_NOT_EXTEND_CLASS = new ApiError("QUERY_NOT_EXTEND_CLASS", 706, "扩展字段没有配置处理类");
    public static final ApiError CFG_QUERY_OPTION_API_CONFIG_REQUIRED = new ApiError("CFG_QUERY_OPTION_API_CONFIG_REQUIRED", 707, "接口路径、下拉框绑定值、下拉框显示值不能为空");
    public static final ApiError CFG_QUERY_OPTION_API_CONFIG_DUPLICATE = new ApiError("CFG_QUERY_OPTION_API_CONFIG_DUPLICATE", 708, "接口路径、下拉框绑定值、下拉框显示值的组合已存在");
    public static final ApiError CFG_FILE_PARSE_NOT_MONTHLY = new ApiError("CFG_FILE_PARSE_NOT_MONTHLY", 3517, "清洗时间仅支持每月");
    public static final ApiError CFG_FILE_PARSE_FOLDER_REQUIRED = new ApiError("CFG_FILE_PARSE_FOLDER_REQUIRED", 3518, "配置文件夹不能为空");
    public static final ApiError CFG_FILE_PARSE_FILE_REQUIRED = new ApiError("CFG_FILE_PARSE_FILE_REQUIRED", 3519, "文件清洗规则不能为空");
    public static final ApiError CFG_FILE_PARSE_DUPLICATE = new ApiError("CFG_FILE_PARSE_DUPLICATE", 3520, "清洗仓库【{0}】清洗时间【{1}】已存在配置");
    public static final ApiError CFG_FILE_PARSE_DOWNSTREAM_EXISTS = new ApiError("CFG_FILE_PARSE_DOWNSTREAM_EXISTS", 3521, "配置【{0}】已存在下游清洗或解析任务，不允许删除");
    public static final ApiError CFG_FILE_PARSE_TEMPLATE_NOT_FOUND = new ApiError("CFG_FILE_PARSE_TEMPLATE_NOT_FOUND", 3522, "月结文件解析配置模板不存在");
    public static final ApiError CFG_FILE_PARSE_EXPORT_TASK_CREATE_FAILED = new ApiError("CFG_FILE_PARSE_EXPORT_TASK_CREATE_FAILED", 3523, "月结文件解析配置导出任务创建失败");
    public static final ApiError CFG_FILE_PARSE_FILE_RULE_INVALID = new ApiError("CFG_FILE_PARSE_FILE_RULE_INVALID", 3524, "第{0}行文件清洗规则不合法：{1}");
    public static final ApiError CFG_FILE_PARSE_FOLDER_RULE_INVALID = new ApiError("CFG_FILE_PARSE_FOLDER_RULE_INVALID", 3525, "第{0}行配置文件夹不合法：{1}");
    public static final ApiError EMAIL_TEMPLATE_NOT_FOUND = new ApiError("EMAIL_TEMPLATE_NOT_FOUND", 2200, "邮箱模板不存在!");
    public static final ApiError EMAIL_CODE_INVALID = new ApiError("EMAIL_CODE_INVALID", 2201, "邮箱验证码错误!");
    public static final ApiError EMAIL_INVALID = new ApiError("EMAIL_INVALID", 2202, "邮箱格式错误!");
    public static final ApiError EMAIL_RATE_LIMITED = new ApiError("EMAIL_RATE_LIMITED", 2203, "发送邮件太频繁 请稍后再试!");
    public static final ApiError EMAIL_SEND_FAILED = new ApiError("EMAIL_SEND_FAILED", 2204, "发送邮件失败!");
    public static final ApiError EMAIL_ADDR_EXISTS = new ApiError("EMAIL_ADDR_EXISTS", 2205, "邮箱已存在");
    public static final ApiError EMAIL_ACCOUN_NOT_BOUND = new ApiError("EMAIL_ACCOUN_NOT_BOUND", 2206, "账号未绑定邮箱，请绑定邮箱后操作");
}
