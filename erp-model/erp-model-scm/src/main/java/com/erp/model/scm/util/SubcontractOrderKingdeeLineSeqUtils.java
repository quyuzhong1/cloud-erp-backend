package com.erp.model.scm.util;

import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 委外订单明细与金蝶分录行号对齐工具。
 * <p>
 * 父行/子行均按 createTime、id 稳定排序，与委外订单推送金蝶及 BOM MQ 中 parentLineSeq、childLineSeq 保持一致。
 */
public final class SubcontractOrderKingdeeLineSeqUtils {

    private static final Comparator<SubcontractOrderDetailEntity> DETAIL_ORDER = Comparator
            .comparing(SubcontractOrderDetailEntity::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(SubcontractOrderDetailEntity::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private SubcontractOrderKingdeeLineSeqUtils() {
    }

    public static List<SubcontractOrderDetailEntity> sortParentDetails(List<SubcontractOrderDetailEntity> details) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyList();
        }
        return details.stream()
                .filter(item -> item != null && StringUtils.isBlank(item.getParentId()))
                .sorted(DETAIL_ORDER)
                .collect(Collectors.toList());
    }

    public static List<SubcontractOrderDetailEntity> sortChildDetails(List<SubcontractOrderDetailEntity> children) {
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }
        return children.stream()
                .filter(item -> item != null)
                .sorted(DETAIL_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * 父行 detailId -> 金蝶 SubReqEntrySeq（从 1 起）。
     */
    public static Map<String, Integer> buildParentLineSeqMap(List<SubcontractOrderDetailEntity> allDetails) {
        List<SubcontractOrderDetailEntity> sortedParents = sortParentDetails(allDetails);
        if (sortedParents.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> parentLineSeqMap = new HashMap<>();
        for (int i = 0; i < sortedParents.size(); i++) {
            SubcontractOrderDetailEntity parentDetail = sortedParents.get(i);
            if (parentDetail != null && StringUtils.isNotBlank(parentDetail.getId())) {
                parentLineSeqMap.put(parentDetail.getId(), i + 1);
            }
        }
        return parentLineSeqMap;
    }

    /**
     * 子行 detailId -> 同父行内行号（从 1 起）。
     */
    public static Map<String, Integer> buildChildLineSeqMap(List<SubcontractOrderDetailEntity> allDetails) {
        if (allDetails == null || allDetails.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<SubcontractOrderDetailEntity>> childrenByParent = allDetails.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.getParentId()))
                .collect(Collectors.groupingBy(SubcontractOrderDetailEntity::getParentId));
        if (childrenByParent.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> childLineSeqMap = new HashMap<>();
        for (List<SubcontractOrderDetailEntity> children : childrenByParent.values()) {
            List<SubcontractOrderDetailEntity> sortedChildren = sortChildDetails(children);
            for (int i = 0; i < sortedChildren.size(); i++) {
                SubcontractOrderDetailEntity childDetail = sortedChildren.get(i);
                if (childDetail != null && StringUtils.isNotBlank(childDetail.getId())) {
                    childLineSeqMap.put(childDetail.getId(), i + 1);
                }
            }
        }
        return childLineSeqMap;
    }
}
