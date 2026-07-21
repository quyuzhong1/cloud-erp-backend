package com.erp.server.tms.service.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.exception.ServiceException;

/**
 * 物流商对账匹配失败原因解析：统一 cause 回溯、业务异常透传与非业务异常脱敏。
 */
public final class LogisticsReconMatchFailReasonSupport {

    /** 与 import_fail_reason / match_fail_reason 等字段长度约定一致 */
    public static final int DEFAULT_MAX_LENGTH = 490;

    /**
     * 机器可识别的不可自动重试前缀；必须保留在失败原因开头。
     * SQL 过滤须用前缀匹配（NOT (LIKE 'prefix%') / not(w -> w.likeRight(...))），禁止 notLike 的 contains 语义。
     */
    public static final String NON_RETRYABLE_PREFIX = "[LRM_NON_RETRYABLE:";

    /** 费用或关联状态可能已经部分提交，重放完整匹配存在重复写风险。 */
    public static final String PARTIAL_COMMIT_PREFIX = NON_RETRYABLE_PREFIX + "PARTIAL_COMMIT] ";

    /** 已进入费用写路径但尚未得到终态；进程崩溃后必须按结果不确定处理。 */
    public static final String EXECUTION_STARTED_REASON = NON_RETRYABLE_PREFIX
            + "EXECUTION_STARTED] 匹配执行中，若任务中断请人工核对费用及关联状态";

    private static final String PARTIAL_COMMIT_SUFFIX = "；费用/关联状态可能已部分落库，请人工核对";

    private static final int MAX_CAUSE_DEPTH = 20;

    private static final String DEFAULT_FAIL_MSG = "匹配失败";

    private static final String GENERIC_FAIL_MSG = "匹配处理异常，请联系管理员";

    private LogisticsReconMatchFailReasonSupport() {
    }

    /**
     * 解析匹配失败原因，默认截断至 {@link #DEFAULT_MAX_LENGTH} 字符。
     */
    public static String resolve(Throwable e) {
        return resolve(e, DEFAULT_MAX_LENGTH);
    }

    /**
     * 解析匹配失败原因；maxLength &lt;= 0 时不截断。
     */
    public static String resolve(Throwable e, int maxLength) {
        if (e == null) {
            return truncate(DEFAULT_FAIL_MSG, maxLength);
        }
        Throwable root = getRootCause(e);
        if (root instanceof ServiceException && CharSequenceUtil.isNotBlank(root.getMessage())) {
            return truncate(root.getMessage(), maxLength);
        }
        return truncate(GENERIC_FAIL_MSG, maxLength);
    }

    /**
     * 构造部分提交场景的不可自动重试原因。
     */
    public static String resolveNonRetryablePartialCommit(Throwable e) {
        int causeMaxLength = DEFAULT_MAX_LENGTH - PARTIAL_COMMIT_PREFIX.length() - PARTIAL_COMMIT_SUFFIX.length();
        return PARTIAL_COMMIT_PREFIX + resolve(e, causeMaxLength) + PARTIAL_COMMIT_SUFFIX;
    }

    /**
     * 判断失败原因是否明确要求人工核对，供认领和错误重试路径统一拦截。
     */
    public static boolean isNonRetryable(String reason) {
        return CharSequenceUtil.isNotBlank(reason) && reason.startsWith(NON_RETRYABLE_PREFIX);
    }

    private static Throwable getRootCause(Throwable e) {
        Throwable root = e;
        int depth = 0;
        while (root.getCause() != null && root.getCause() != root && depth < MAX_CAUSE_DEPTH) {
            root = root.getCause();
            depth++;
        }
        return root;
    }

    private static String truncate(String msg, int maxLength) {
        if (CharSequenceUtil.isBlank(msg)) {
            return msg;
        }
        if (maxLength <= 0 || msg.length() <= maxLength) {
            return msg;
        }
        return msg.substring(0, maxLength);
    }
}
