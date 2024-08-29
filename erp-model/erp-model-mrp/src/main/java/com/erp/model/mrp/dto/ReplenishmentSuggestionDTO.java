package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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


    /**
     * 暂不补货/恢复补货
     */
    @Data
    @NoArgsConstructor
    public static class ReplenishmentDTO {
        /**
         * 主键ids
         */
        @NotBlank(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 补货原因
         */
        @NotBlank(message = "原因说明不能为空")
        private String replenishmentRemark;
     }

    /**
     * 批量设置规则参数
     */
    @Data
    @NoArgsConstructor
    public static class UpdateRuleDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 备货设置
         */
        @Valid
        private CfgRuleStockUpDTO.UpdateDTO stockUpUpdateDTO;

        /**
         * 销量设置
         */
        @Valid
        private CfgRuleSalesQtyDTO.UpdateDTO salesQtyUpdateDTO;

    }

    /**
     * 批量设置规则参数
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateRuleDTO {

        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 备货设置
         */
        @Valid
        private CfgRuleStockUpDTO.UpdateDTO stockUpUpdateDTO;

       /**
        * 销量设置
        */
       @Valid
       private CfgRuleSalesQtyDTO.UpdateDTO salesQtyUpdateDTO;

    }

    /**
     * 恢复规则
     */
    @Data
    @NoArgsConstructor
    public static class RestoreRuleDTO {

        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 规则选项
         */
        @NotEmpty(message = "规则选项不能为空")
        private List<String> ruleTypeList;
    }

}
