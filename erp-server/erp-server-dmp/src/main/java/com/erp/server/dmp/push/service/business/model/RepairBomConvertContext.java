package com.erp.server.dmp.push.service.business.model;

import cn.hutool.json.JSONObject;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 委外用料清单变更单转换上下文：统一旧单/新单父行匹配结果与 SCM 仓库字段映射。
 * <p>
 * {@link #matchedGroups} 经金蝶分录行号收窄后，通常仅含当前 BOM 对应的单个父行；
 * {@link #parentEntrySeqMap} 供子行匹配时在 kingdeeDetailId 缺失场景下按 SubReqEntrySeq 消歧，优先来自 MQ parentLineSeq；
 * {@link #childLineSeqMap} 供同 SKU 子行按金蝶 Seq/ReplaceGroup 与 childLineSeq 消歧。
 */
public class RepairBomConvertContext {

    private final Map<String, JSONObject> detailStockFieldMap;
    private final List<ParentChildGroup> matchedGroups;
    private final List<SubcontractOrderDetailEntity> changeChildDetails;
    private final Set<String> matchedParentIds;
    /** 父行 detailId -> 金蝶委外订单分录行号（SubReqEntrySeq，从 1 起），用于分录内码缺失时的兜底匹配 */
    private final Map<String, Integer> parentEntrySeqMap;
    /** 委外用料清单编号（MQ FBillNo），供歧义匹配 warn 日志检索 */
    private final String bomBillNo;
    /** 子行 detailId -> 同父行内行号（childLineSeq，从 1 起），用于同 SKU 子行消歧 */
    private final Map<String, Integer> childLineSeqMap;

    public RepairBomConvertContext(Map<String, JSONObject> detailStockFieldMap,
            List<ParentChildGroup> matchedGroups) {
        this(detailStockFieldMap, matchedGroups, Collections.emptyMap(), null, Collections.emptyMap());
    }

    public RepairBomConvertContext(Map<String, JSONObject> detailStockFieldMap,
            List<ParentChildGroup> matchedGroups, Map<String, Integer> parentEntrySeqMap) {
        this(detailStockFieldMap, matchedGroups, parentEntrySeqMap, null, Collections.emptyMap());
    }

    public RepairBomConvertContext(Map<String, JSONObject> detailStockFieldMap,
            List<ParentChildGroup> matchedGroups, Map<String, Integer> parentEntrySeqMap, String bomBillNo) {
        this(detailStockFieldMap, matchedGroups, parentEntrySeqMap, bomBillNo, Collections.emptyMap());
    }

    /**
     * @param parentEntrySeqMap SCM 父行与金蝶 SubReqEntrySeq 的映射，优先来自 MQ list 的 parentLineSeq
     * @param bomBillNo 委外用料清单编号
     * @param childLineSeqMap SCM 子行与同父行内 childLineSeq 的映射
     */
    public RepairBomConvertContext(Map<String, JSONObject> detailStockFieldMap,
            List<ParentChildGroup> matchedGroups, Map<String, Integer> parentEntrySeqMap, String bomBillNo,
            Map<String, Integer> childLineSeqMap) {
        this.detailStockFieldMap = detailStockFieldMap == null ? Collections.emptyMap() : detailStockFieldMap;
        this.matchedGroups = matchedGroups == null
                ? Collections.emptyList()
                : matchedGroups.stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.changeChildDetails = this.matchedGroups.stream()
                .map(ParentChildGroup::getChildDetails)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.matchedParentIds = this.matchedGroups.stream()
                .map(ParentChildGroup::getParentDetail)
                .filter(Objects::nonNull)
                .map(SubcontractOrderDetailEntity::getId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());
        this.parentEntrySeqMap = parentEntrySeqMap == null ? Collections.emptyMap() : parentEntrySeqMap;
        this.bomBillNo = bomBillNo;
        this.childLineSeqMap = childLineSeqMap == null ? Collections.emptyMap() : childLineSeqMap;
    }

    public Map<String, JSONObject> getDetailStockFieldMap() {
        return detailStockFieldMap;
    }

    public List<ParentChildGroup> getMatchedGroups() {
        return matchedGroups;
    }

    public List<SubcontractOrderDetailEntity> getChangeChildDetails() {
        return changeChildDetails;
    }

    public Set<String> getMatchedParentIds() {
        return matchedParentIds;
    }

    public Map<String, Integer> getParentEntrySeqMap() {
        return parentEntrySeqMap;
    }

    public String getBomBillNo() {
        return bomBillNo;
    }

    public Map<String, Integer> getChildLineSeqMap() {
        return childLineSeqMap;
    }

    public static class ParentChildGroup {

        private final SubcontractOrderDetailEntity parentDetail;
        private final List<SubcontractOrderDetailEntity> childDetails;

        public ParentChildGroup(SubcontractOrderDetailEntity parentDetail,
                List<SubcontractOrderDetailEntity> childDetails) {
            if (parentDetail == null) {
                throw new IllegalArgumentException("parentDetail must not be null");
            }
            this.parentDetail = parentDetail;
            if (childDetails == null || childDetails.isEmpty()) {
                this.childDetails = Collections.emptyList();
            } else {
                List<SubcontractOrderDetailEntity> filteredChildren = childDetails.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                this.childDetails = Collections.unmodifiableList(filteredChildren);
            }
        }

        public SubcontractOrderDetailEntity getParentDetail() {
            return parentDetail;
        }

        public List<SubcontractOrderDetailEntity> getChildDetails() {
            return childDetails;
        }
    }

}
