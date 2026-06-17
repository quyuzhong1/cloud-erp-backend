package com.erp.model.tms.dto;

import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 物流商对账匹配编排结果（Feign 匹配与结果落库分离）。
 *
 * @author Will
 * @since 2026-06-15
 */
@Getter
public class LogisticsReconMatchExecutionResultDTO {

    /** 逐行（或逐费用项）匹配结果 */
    private final List<LogisticsReconMatchDTO.MatchResultDTO> matchResults;

    /** 行键 → 对账明细 id */
    private final Map<String, String> rowKeyToDetailId;

    /** 行键 → 参与匹配的费用项列表 */
    private final Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs;

    /**
     * @param matchResults     匹配服务返回的逐单元结果
     * @param rowKeyToDetailId 行键与明细 id 映射
     * @param rowKeyToSubs     行键与费用项列表映射
     */
    public LogisticsReconMatchExecutionResultDTO(List<LogisticsReconMatchDTO.MatchResultDTO> matchResults,
                                                 Map<String, String> rowKeyToDetailId,
                                                 Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs) {
        this.matchResults = matchResults;
        this.rowKeyToDetailId = rowKeyToDetailId;
        this.rowKeyToSubs = rowKeyToSubs;
    }
}
