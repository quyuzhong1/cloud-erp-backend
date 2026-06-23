package com.erp.server.dmp.inout.handler.input.task.dmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    public void shouldNotTreatRefundRejectAsRefundEvenIfSellerCreatedReturnAndRefundSolution() {
        Map<String, Object> issueDetail = new HashMap<>();
        issueDetail.put("reverse_detail_status", "refund_reject");
        issueDetail.put("buyer_solution_list",
                solutionList(solution("refund", "44.44", "USD", true, "wait_seller_accept", "4009293259379837")));
        issueDetail.put("seller_solution_list",
                solutionList(solution("return_and_refund", "44.44", "USD", false, "reached", "4009266332479837")));

        AliExpressIssueSolutionResolver.ResolvedIssueSolution resolved = AliExpressIssueSolutionResolver.resolve(issueDetail);

        assertFalse(resolved.isMatchedReturn());
        assertFalse(resolved.isMatchedRefund());
        assertNull(resolved.getEffectiveSolution());
    }

    @Test
    public void shouldNotTreatProcessingRefundSolutionAsRefund() {
        Map<String, Object> issueDetail = new HashMap<>();
        issueDetail.put("issue_status", "processing");
        issueDetail.put("reverse_detail_status", "wait_for_AE_feedback");
        issueDetail.put("buyer_solution_list",
                solutionList(solution("refund", "138.78", "PEN", true, "wait_seller_accept", "6014855219364413")));

        AliExpressIssueSolutionResolver.ResolvedIssueSolution resolved = AliExpressIssueSolutionResolver.resolve(issueDetail);

        assertFalse(resolved.isMatchedReturn());
        assertFalse(resolved.isMatchedRefund());
        assertNull(resolved.getEffectiveSolution());
    }

    @Test
    public void shouldFallbackTrackingNumberToBuyerReturnNo() {
        Map<String, Object> issueDetail = new HashMap<>();
        issueDetail.put("buyer_return_no", "DPD123456");

        assertEquals("DPD123456", AliExpressIssueSolutionResolver.getReturnTrackingNo(issueDetail));

        issueDetail.put("buyer_return_logistics_lp_no", "LP654321");
        assertEquals("LP654321", AliExpressIssueSolutionResolver.getReturnTrackingNo(issueDetail));
    }

    @Test
    public void shouldKeepFullTextForTextColumnAndTrimOnlyVarcharFields() {
        String longText = repeat('a', 260);

        assertEquals(longText, AliExpressIssueSolutionResolver.pickIssueText(longText));
        assertEquals(repeat('a', 255), AliExpressIssueSolutionResolver.pickIssueTextForVarchar(longText));
    }

    @Test
    public void shouldRejectParagraphAsTrackingNumber() {
        Map<String, Object> issueDetail = new HashMap<>();
        issueDetail.put("buyer_return_no", "Я не буду повертати товар. Ви мені дали варіант лише повернення коштів.");

        assertEquals("", AliExpressIssueSolutionResolver.getReturnTrackingNo(issueDetail));
    }

    @Test
    public void shouldMapRefundStatusByReverseDetailStatus() {
        assertEquals("1", AliExpressIssueSolutionResolver.resolveRefundStatus("refund_success", "finish"));
        assertEquals("2", AliExpressIssueSolutionResolver.resolveRefundStatus("refund_reject", "finish"));
        assertEquals("3", AliExpressIssueSolutionResolver.resolveRefundStatus("processing", "canceled_issue"));
        assertEquals("", AliExpressIssueSolutionResolver.resolveRefundStatus("wait_for_AE_feedback", "processing"));
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

    private String repeat(char ch, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, ch);
        return new String(chars);
    }
}
