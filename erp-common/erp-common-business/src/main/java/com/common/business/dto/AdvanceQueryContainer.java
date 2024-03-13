package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 高级筛选通用查询DTO
 * @Author LiuRuiPeng
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvanceQueryContainer {

    /**
     * 页面高级查询
     */
    private List<AdvanceQueryDTO> advanceQueryDTOList;

    /**
     * sqlMap 默认key default
     */
    private Map<String, String> sqlMap;
}
