package com.erp.server.tms.service.support;

import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对账导入匹配任务上下文：按识别号分组后的账单费用项 id（与导入分组匹配对齐）。
 *
 * @author Will
 * @date 2026/6/12
 */
@Getter
public class LogisticsReconImportMatchContext {

    private final String mainId;

    private final List<CfgLogisticsCostImportDetailEntity> uniqueKeyList;

    /**
     * 识别号分组键 → 该组下全部费用项 id（含不可匹配状态）
     */
    private final Map<String, List<String>> groupKeyToAllSubIds;

    /**
     * 识别号分组键 → 该组下可参与匹配的费用项 id
     */
    private final Map<String, List<String>> groupKeyToEligibleSubIds;

    public LogisticsReconImportMatchContext(String mainId,
                                            List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                            Map<String, List<String>> groupKeyToAllSubIds,
                                            Map<String, List<String>> groupKeyToEligibleSubIds) {
        this.mainId = mainId;
        this.uniqueKeyList = uniqueKeyList == null ? Collections.emptyList() : uniqueKeyList;
        this.groupKeyToAllSubIds = groupKeyToAllSubIds == null ? Collections.emptyMap() : groupKeyToAllSubIds;
        this.groupKeyToEligibleSubIds = groupKeyToEligibleSubIds == null ? Collections.emptyMap() : groupKeyToEligibleSubIds;
    }
}
