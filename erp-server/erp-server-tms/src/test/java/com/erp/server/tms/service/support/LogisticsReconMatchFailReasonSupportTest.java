package com.erp.server.tms.service.support;

import com.common.core.exception.ServiceException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LogisticsReconMatchFailReasonSupportTest {

    @Test
    public void partialCommitReasonShouldBeStableMachineReadableAndLengthBounded() {
        String reason = LogisticsReconMatchFailReasonSupport.resolveNonRetryablePartialCommit(
                new ServiceException(repeat("业务失败", 300)));

        assertTrue(reason.startsWith(LogisticsReconMatchFailReasonSupport.PARTIAL_COMMIT_PREFIX));
        assertTrue(LogisticsReconMatchFailReasonSupport.isNonRetryable(reason));
        assertTrue(reason.length() <= LogisticsReconMatchFailReasonSupport.DEFAULT_MAX_LENGTH);
        assertTrue(reason.endsWith("费用/关联状态可能已部分落库，请人工核对"));
    }

    @Test
    public void ordinaryFailureShouldRemainRetryable() {
        assertTrue(LogisticsReconMatchFailReasonSupport.isNonRetryable(
                LogisticsReconMatchFailReasonSupport.EXECUTION_STARTED_REASON));
        assertFalse(LogisticsReconMatchFailReasonSupport.isNonRetryable("获取对账单锁超时，匹配未执行"));
        assertFalse(LogisticsReconMatchFailReasonSupport.isNonRetryable(null));
    }

    @Test
    public void markerInMiddleOfReasonShouldRemainRetryable() {
        assertFalse(LogisticsReconMatchFailReasonSupport.isNonRetryable(
                "下游返回 [LRM_NON_RETRYABLE: 内容，但本次仍可重试"));
    }

    private String repeat(String value, int times) {
        StringBuilder result = new StringBuilder(value.length() * times);
        for (int i = 0; i < times; i++) {
            result.append(value);
        }
        return result.toString();
    }
}
