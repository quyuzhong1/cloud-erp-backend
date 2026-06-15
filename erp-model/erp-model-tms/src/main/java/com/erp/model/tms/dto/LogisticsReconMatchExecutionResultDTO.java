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

    private final List<LogisticsReconMatchDTO.MatchResultDTO> matchResults;
    private final Map<String, String> rowKeyToDetailId;
    private final Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs;

    public LogisticsReconMatchExecutionResultDTO(List<LogisticsReconMatchDTO.MatchResultDTO> matchResults,
                                                 Map<String, String> rowKeyToDetailId,
                                                 Map<String, List<LogisticsReconDetailSubEntity>> rowKeyToSubs) {
        this.matchResults = matchResults;
        this.rowKeyToDetailId = rowKeyToDetailId;
        this.rowKeyToSubs = rowKeyToSubs;
    }
}
