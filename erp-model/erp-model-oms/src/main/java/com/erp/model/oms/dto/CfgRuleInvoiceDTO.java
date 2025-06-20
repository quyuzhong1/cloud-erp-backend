package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 开票规则请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-05-23
*/
@Data
@NoArgsConstructor
public class CfgRuleInvoiceDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 规则名称
        */
        private String name;

        /**
        * 禁用状态 false 未禁用
        */
        private Boolean disabled;

        /**
        * 描述
        */
        private String remark;

        /**
        * 优先级
        */
        private Integer priority;

        /**
        * 发票类型:vat=VAT发票,nfe=NF-e发票
        */
        private String invoiceType;
        /**
         * 条件
         */
        private List<RuleConditionDTO.ViewDTO> conditionList;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        private List<RuleConditionDTO.AddDTO> conditionList;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;
        private List<RuleConditionDTO.UpdateDTO> conditionList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 规则名称
        */
        @NotBlank(message = "规则名称不能为空")
        @Size(max = 50,message = "规则名称最大长度不能超过50位")
        private String name;

        /**
        * 禁用状态 false 未禁用
        */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;

        /**
        * 描述
        */
        @Size(max = 255,message = "描述最大长度不能超过255位")
        private String remark;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        /**
        * 发票类型:vat=VAT发票,nfe=NF-e发票
        */
        @NotBlank(message = "发票类型不能为空")
        @Size(max = 30,message = "发票类型最大长度不能超过30位")
        private String invoiceType;


    }


    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 规则名称
         */
        private String name;

        /**
         * 禁用状态 false 未禁用
         */
        private Boolean disabled;

        /**
         * 描述
         */
        private String remark;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 发票类型:vat=VAT发票,nfe=NF-e发票
         */
        private String invoiceType;
        private String invoiceTypeName;
        /**
         * 更新人
         */
        private String updateUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }
}