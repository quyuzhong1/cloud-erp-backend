package com.erp.server.dmp.push.service.business.model;

import cn.hutool.json.JSONObject;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 委外用料清单变更单转换上下文：统一旧单/新单父行匹配结果与 SCM 仓库字段映射。
 */
public class RepairBomConvertContext {

    private final Map<String, JSONObject> detailStockFieldMap;
    private final List<ParentChildGroup> matchedGroups;
    private final List<SubcontractOrderDetailEntity> changeChildDetails;
    private final Set<String> matchedParentIds;

    public RepairBomConvertContext(Map<String, JSONObject> detailStockFieldMap,
            List<ParentChildGroup> matchedGroups) {
        this.detailStockFieldMap = detailStockFieldMap;
        this.matchedGroups = matchedGroups == null ? Collections.emptyList() : matchedGroups;
        this.changeChildDetails = this.matchedGroups.stream()
                .flatMap(group -> group.getChildDetails().stream())
                .collect(Collectors.toList());
        this.matchedParentIds = this.matchedGroups.stream()
                .map(group -> group.getParentDetail().getId())
                .collect(Collectors.toSet());
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

    public static class ParentChildGroup {

        private final SubcontractOrderDetailEntity parentDetail;
        private final List<SubcontractOrderDetailEntity> childDetails;

        public ParentChildGroup(SubcontractOrderDetailEntity parentDetail,
                List<SubcontractOrderDetailEntity> childDetails) {
            this.parentDetail = parentDetail;
            this.childDetails = childDetails;
        }

        public SubcontractOrderDetailEntity getParentDetail() {
            return parentDetail;
        }

        public List<SubcontractOrderDetailEntity> getChildDetails() {
            return childDetails;
        }
    }

}
