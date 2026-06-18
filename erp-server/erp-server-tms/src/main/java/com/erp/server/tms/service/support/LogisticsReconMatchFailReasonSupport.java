package com.erp.server.tms.service.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.exception.ServiceException;

/**
 * 物流商对账匹配失败原因解析：统一 cause 回溯、业务异常透传与非业务异常脱敏。
 */
public final class LogisticsReconMatchFailReasonSupport {

    /** 与 import_fail_reason / match_fail_reason 等字段长度约定一致 */
    public static final int DEFAULT_MAX_LENGTH = 490;

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
