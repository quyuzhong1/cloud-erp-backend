package com.erp.server.dmp.inout.handler.input.task.dmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class AliExpressIssueSolutionResolverTest {

    @Test
    public void shouldTreatBuyerReturnAndRefundAsReturnEvenIfPlatformOnlyHasRefund() {
        Map<String, Object> issueDetail = new HashMap<>();
        issueDetail.put("reverse_detail_status", "return_success");
        issueDetail.put("buyer_solution_list",
                solutionList(solution("return_and_refund", "28.59", "EUR", true, "reached", "4010330826992288")));
        issueDetail.put("platform_solution_list",
                solutionList(solution("refund", "28.59", "EUR", false, "", "-2")));

        AliExpressIssueSolutionResolver.ResolvedIssueSolution resolved = AliExpressIssueSolutionResolver.resolve(issueDetail);

        assertTrue(resolved.isMatchedReturn());
        assertFalse(resolved.isMatchedRefund());
        assertNotNull(resolved.getEffectiveSolution());
        assertEquals(AliExpressIssueSolutionResolver.SOLUTION_RETURN_AND_REFUND,
                resolved.getEffectiveSolution().getSolutionType());
        assertEquals("4010330826992288", resolved.getEffectiveSolution().get("id"));
    }

    @Test
    public void shouldTreatBuyerRefundSuccessAsRefundWithoutPlatformSolution() {
        Map<String, Object> issueDetail = new HashMap<>();
        issueDetail.put("reverse_detail_status", "refund_success");
        issueDetail.put("buyer_solution_list",
                solutionList(solution("refund", "15.59", "EUR", true, "wait_seller_accept", "4009992537418916")));

        AliExpressIssueSolutionResolver.ResolvedIssueSolution resolved = AliExpressIssueSolutionResolver.resolve(issueDetail);

        assertFalse(resolved.isMatchedReturn());
        assertTrue(resolved.isMatchedRefund());
        assertNotNull(resolved.getEffectiveSolution());
        assertEquals(AliExpressIssueSolutionResolver.SOLUTION_REFUND, resolved.getEffectiveSolution().getSolutionType());
        assertEquals("15.59", resolved.getEffectiveSolution().get("refund_money"));
    }

    @Test
    public void shouldFallbackTrackingNumberToBuyerReturnNo() {
        Map<String, Object> issueDetail = new HashMap<>();
        issueDetail.put("buyer_return_no", "DPD123456");

        assertEquals("DPD123456", AliExpressIssueSolutionResolver.getReturnTrackingNo(issueDetail));

        issueDetail.put("buyer_return_logistics_lp_no", "LP654321");
        assertEquals("LP654321", AliExpressIssueSolutionResolver.getReturnTrackingNo(issueDetail));
    }

    private Map<String, Object> solutionList(Map<String, Object>... solutions) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("solution_api_dto", Arrays.asList(solutions));
        return wrapper;
    }

    private Map<String, Object> solution(String solutionType, String refundMoney, String refundCurrency,
            boolean isDefault, String status, String id) {
        Map<String, Object> solution = new HashMap<>();
        solution.put("solution_type", solutionType);
        solution.put("refund_money", refundMoney);
        solution.put("refund_money_currency", refundCurrency);
        solution.put("is_default", isDefault);
        solution.put("status", status);
        solution.put("id", id);
        return solution;
    }
}
