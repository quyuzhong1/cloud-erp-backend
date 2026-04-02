package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;

final class AliExpressIssueSolutionResolver {
    static final String SOLUTION_RETURN_AND_REFUND = "return_and_refund";
    static final String SOLUTION_REFUND = "refund";
    static final String REVERSE_STATUS_REFUND_SUCCESS = "refund_success";
    static final String REVERSE_STATUS_REFUND_REJECT = "refund_reject";
    private static final int DEFAULT_DB_TEXT_MAX_LENGTH = 255;
    private static final Set<String> RETURN_REVERSE_STATUSES = new HashSet<>(Arrays.asList(
            "wait_for_seller_set_return_address",
            "wait_for_buyer_returnid",
            "wait_for_seller_confirm",
            "wait_for_warehouse_confirmation",
            "wait_for_warehouse_quality_check",
            "return_success"));
    private static final Set<String> REFUND_REVERSE_STATUSES = new HashSet<>(Arrays.asList(
            REVERSE_STATUS_REFUND_SUCCESS,
            REVERSE_STATUS_REFUND_REJECT));

    private static final List<String> SOLUTION_LIST_KEYS = Arrays.asList(
            "buyer_solution_list",
            "platform_solution_list",
            "seller_solution_list");

    private static final Comparator<SolutionRecord> SOLUTION_COMPARATOR = Comparator
            .comparingInt(AliExpressIssueSolutionResolver::statusRank)
            .thenComparing((SolutionRecord solution) -> !solution.isDefault)
            .thenComparingInt(AliExpressIssueSolutionResolver::ownerRank)
            .thenComparingInt(solution -> solution.sortOrder);

    private AliExpressIssueSolutionResolver() {
    }

    static ResolvedIssueSolution resolve(Map<String, Object> issueDetail) {
        String reverseDetailStatus = getLowerCase(issueDetail.get("reverse_detail_status"));
        List<SolutionRecord> candidates = collectSolutions(issueDetail);
        SolutionRecord returnSolution = pickSolution(candidates, SOLUTION_RETURN_AND_REFUND);
        SolutionRecord refundSolution = pickSolution(candidates, SOLUTION_REFUND);
        boolean hasReturnSolution = returnSolution != null;
        boolean hasRefundSolution = refundSolution != null;

        boolean matchedReturn = false;
        boolean matchedRefund = false;
        if (hasReturnSolution && isReturnReverseStatus(reverseDetailStatus)) {
            matchedReturn = true;
        } else if (hasRefundSolution && isRefundReverseStatus(reverseDetailStatus)) {
            matchedRefund = true;
        } else if (hasRefundSolution && !hasReturnSolution) {
            matchedRefund = true;
        } else if (hasReturnSolution) {
            matchedReturn = true;
        }

        SolutionRecord effectiveSolution = matchedReturn ? returnSolution : (matchedRefund ? refundSolution : null);
        return new ResolvedIssueSolution(reverseDetailStatus, matchedReturn, matchedRefund, effectiveSolution);
    }

    static boolean isReturnReverseStatus(String reverseDetailStatus) {
        return RETURN_REVERSE_STATUSES.contains(getLowerCase(reverseDetailStatus));
    }

    static boolean isRefundReverseStatus(String reverseDetailStatus) {
        return REFUND_REVERSE_STATUSES.contains(getLowerCase(reverseDetailStatus));
    }

    static String resolveRefundStatus(String reverseDetailStatus, String issueStatus) {
        String normalizedReverseStatus = getLowerCase(reverseDetailStatus);
        if (REVERSE_STATUS_REFUND_SUCCESS.equals(normalizedReverseStatus)) {
            return "1";
        }
        if (REVERSE_STATUS_REFUND_REJECT.equals(normalizedReverseStatus)) {
            return "2";
        }
        if ("canceled_issue".equals(getLowerCase(issueStatus))) {
            return "3";
        }
        return "1";
    }

    static String getReturnTrackingNo(Map<String, Object> issueDetail) {
        return sanitizeTrackingNo(firstNonBlank(issueDetail.get("buyer_return_logistics_lp_no"), issueDetail.get("buyer_return_no")));
    }

    static String getIssueContent(Map<String, Object> issueDetail) {
        for (SolutionRecord solution : collectSolutions(issueDetail)) {
            String content = firstNonBlank(solution.get("content"));
            if (StringUtils.isNotBlank(content)) {
                return content;
            }
        }

        Object processDtoListObj = issueDetail.get("process_dto_list");
        if (!(processDtoListObj instanceof Map)) {
            return "";
        }
        Object issueProcessDtoObj = ((Map<String, Object>) processDtoListObj).get("api_issue_process_dto");
        if (!(issueProcessDtoObj instanceof List)) {
            return "";
        }

        String fallbackContent = "";
        for (Object item : (List<?>) issueProcessDtoObj) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<String, Object> process = (Map<String, Object>) item;
            String content = firstNonBlank(process.get("content"));
            if (StringUtils.isBlank(content)) {
                continue;
            }
            if ("buyer".equals(getLowerCase(process.get("submit_member_type")))) {
                return content;
            }
            if (StringUtils.isBlank(fallbackContent)) {
                fallbackContent = content;
            }
        }
        return fallbackContent;
    }

    static String getLatestEventTime(Map<String, Object> issueDetail) {
        String latestTime = firstNonBlank(issueDetail.get("gmt_modified"), issueDetail.get("gmt_create"));
        for (SolutionRecord solution : collectSolutions(issueDetail)) {
            latestTime = maxTime(latestTime, firstNonBlank(solution.get("gmt_modified"), solution.get("gmt_create")));
        }

        Object processDtoListObj = issueDetail.get("process_dto_list");
        if (processDtoListObj instanceof Map) {
            Object issueProcessDtoObj = ((Map<String, Object>) processDtoListObj).get("api_issue_process_dto");
            if (issueProcessDtoObj instanceof List) {
                for (Object item : (List<?>) issueProcessDtoObj) {
                    if (!(item instanceof Map)) {
                        continue;
                    }
                    latestTime = maxTime(latestTime, firstNonBlank(((Map<String, Object>) item).get("gmt_create")));
                }
            }
        }
        return latestTime;
    }

    static String pickIssueText(Object... values) {
        return normalizeText(firstNonBlank(values));
    }

    static String pickIssueTextForVarchar(Object... values) {
        return trimForDb(firstNonBlank(values));
    }

    static String trimForDb(Object value) {
        return StringUtils.left(normalizeText(value), DEFAULT_DB_TEXT_MAX_LENGTH);
    }

    static String normalizeText(Object value) {
        String text = ObjectUtil.defaultIfNull(value, "").toString();
        return ObjectUtil.defaultIfNull(StringUtils.normalizeSpace(text), "").toString();
    }

    static String sanitizeTrackingNo(Object value) {
        String trackingNo = normalizeText(value);
        if (StringUtils.isBlank(trackingNo)) {
            return "";
        }
        if (trackingNo.length() > DEFAULT_DB_TEXT_MAX_LENGTH) {
            return "";
        }
        return trackingNo.matches("^[A-Za-z0-9][A-Za-z0-9\\-_.:/]*$") ? trackingNo : "";
    }

    private static List<SolutionRecord> collectSolutions(Map<String, Object> issueDetail) {
        List<SolutionRecord> candidates = new ArrayList<>();
        int sortOrder = 0;
        for (String solutionListKey : SOLUTION_LIST_KEYS) {
            Object solutionListObj = issueDetail.get(solutionListKey);
            if (!(solutionListObj instanceof Map)) {
                continue;
            }
            Object solutionApiDtoObj = ((Map<String, Object>) solutionListObj).get("solution_api_dto");
            if (!(solutionApiDtoObj instanceof List)) {
                continue;
            }
            for (Object item : (List<?>) solutionApiDtoObj) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<String, Object> rawSolution = (Map<String, Object>) item;
                String solutionType = getLowerCase(rawSolution.get("solution_type"));
                if (StringUtils.isBlank(solutionType)) {
                    continue;
                }
                candidates.add(new SolutionRecord(solutionListKey, rawSolution, solutionType, sortOrder++));
            }
        }
        return candidates;
    }

    private static SolutionRecord pickSolution(List<SolutionRecord> candidates, String targetType) {
        if (CollUtil.isEmpty(candidates)) {
            return null;
        }
        return candidates.stream()
                .filter(solution -> targetType.equals(solution.solutionType))
                .sorted(SOLUTION_COMPARATOR)
                .findFirst()
                .orElse(null);
    }

    private static int statusRank(SolutionRecord solution) {
        if ("reached".equals(solution.status)) {
            return 0;
        }
        if ("wait_buyer_accept".equals(solution.status)) {
            return 1;
        }
        if ("wait_seller_accept".equals(solution.status)) {
            return 2;
        }
        if (StringUtils.isBlank(solution.status)) {
            return 4;
        }
        return 3;
    }

    private static int ownerRank(SolutionRecord solution) {
        if ("platform".equals(solution.solutionOwner)) {
            return 0;
        }
        if ("seller".equals(solution.solutionOwner)) {
            return 1;
        }
        return 2;
    }

    private static String getLowerCase(Object value) {
        return ObjectUtil.defaultIfNull(value, "").toString().toLowerCase();
    }

    private static String firstNonBlank(Object... values) {
        for (Object value : values) {
            String text = ObjectUtil.defaultIfNull(value, "").toString();
            if (StringUtils.isNotBlank(text)) {
                return text;
            }
        }
        return "";
    }

    private static String maxTime(String current, String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return current;
        }
        if (StringUtils.isBlank(current)) {
            return candidate;
        }
        return current.compareTo(candidate) >= 0 ? current : candidate;
    }

    static final class ResolvedIssueSolution {
        private final String reverseDetailStatus;
        private final boolean matchedReturn;
        private final boolean matchedRefund;
        private final SolutionRecord effectiveSolution;

        private ResolvedIssueSolution(String reverseDetailStatus, boolean matchedReturn, boolean matchedRefund,
                SolutionRecord effectiveSolution) {
            this.reverseDetailStatus = reverseDetailStatus;
            this.matchedReturn = matchedReturn;
            this.matchedRefund = matchedRefund;
            this.effectiveSolution = effectiveSolution;
        }

        String getReverseDetailStatus() {
            return reverseDetailStatus;
        }

        boolean isMatchedReturn() {
            return matchedReturn;
        }

        boolean isMatchedRefund() {
            return matchedRefund;
        }

        SolutionRecord getEffectiveSolution() {
            return effectiveSolution;
        }
    }

    static final class SolutionRecord {
        private final String solutionListKey;
        private final String solutionOwner;
        private final Map<String, Object> rawSolution;
        private final String solutionType;
        private final String status;
        private final boolean isDefault;
        private final int sortOrder;

        private SolutionRecord(String solutionListKey, Map<String, Object> rawSolution, String solutionType, int sortOrder) {
            this.solutionListKey = solutionListKey;
            this.solutionOwner = StringUtils.substringBefore(solutionListKey, "_solution_list");
            this.rawSolution = rawSolution;
            this.solutionType = solutionType;
            this.status = getLowerCase(rawSolution.get("status"));
            this.isDefault = Boolean.TRUE.equals(rawSolution.get("is_default"));
            this.sortOrder = sortOrder;
        }

        Object get(String key) {
            return rawSolution.get(key);
        }

        String getSolutionType() {
            return solutionType;
        }
    }
}
