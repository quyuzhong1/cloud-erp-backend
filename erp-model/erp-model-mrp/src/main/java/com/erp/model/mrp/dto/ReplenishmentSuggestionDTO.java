package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ReplenishmentSuggestionDTO implements Serializable {


    @Getter
    @Setter
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
        /**
         * 建议类型
         */
        @NotBlank(message = "建议类型不能为空")
        private String platformType;
        /**
         * sku类型
         */
        @NotBlank(message = "sku类型不能为空")
        private String skuType;
        /**
         * 是否关注
         */
        private Boolean favorite;
    }
}
