package com.erp.model.plm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Data
@NoArgsConstructor
public class ProjectScheduleDTO implements Serializable {


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

    }
}