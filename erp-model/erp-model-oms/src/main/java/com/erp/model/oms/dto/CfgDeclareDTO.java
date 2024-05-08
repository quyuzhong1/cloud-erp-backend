package com.erp.model.oms.dto;

import java.math.BigDecimal;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;

/**
 * <p>
 * 申报规则表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
*/
@Data
@NoArgsConstructor
public class CfgDeclareDTO implements Serializable {




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
        * 报关中文名称
        */
        private String declareChineseName;

        /**
        * 报关英文名称
        */
        private String declareEnglishName;

        /**
        * 目的国海关编码
        */
        private String toCustomsCode;

        /**
        * 优先级
        */
        private Integer priority;

        /**
        * 目的国申报价
        */
        private BigDecimal toDeclarePrice;

        /**
        * 目的国申报币种
        */
        private String toCurrency;

        /**
        * 目的国申报币种符号
        */
        private String toCurrencySymbol;

        /**
        * 目的国申报类型（dictType=toDeclarePriceType）
        */
        private String toDeclarePriceType;

        /**
        * 固定比例（当toDeclarePriceType=ratePrice）必填
        */
        private BigDecimal rate;

        /**
        * 最高申报价
        */
        private BigDecimal maxDeclarePrice;

        /**
        * 最低申报价
        */
        private BigDecimal minDeclarePrice;


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
        @NotNull(message = "禁用状态 false 未禁用不能为空")
        private Boolean disabled;

        /**
        * 描述
        */
//        @NotBlank(message = "描述不能为空")
        @Size(max = 255,message = "描述最大长度不能超过200位")
        private String remark;

        /**
        * 报关中文名称
        */
//        @NotBlank(message = "报关中文名称不能为空")
        @Size(max = 255,message = "报关中文名称最大长度不能超过255位")
        private String declareChineseName;

        /**
        * 报关英文名称
        */
//        @NotBlank(message = "报关英文名称不能为空")
        @Size(max = 255,message = "报关英文名称最大长度不能超过255位")
        private String declareEnglishName;

        /**
        * 目的国海关编码
        */
//        @NotBlank(message = "目的国海关编码不能为空")
        @Size(max = 100,message = "目的国海关编码最大长度不能超过100位")
        private String toCustomsCode;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        @DecimalMin(value = "0", message = "最小值为1")
        @DecimalMax(value = "10", message = "最小值为10")
        private Integer priority;

        /**
        * 目的国申报价
        */
//        @NotNull(message = "目的国申报价不能为空")
        @Digits(integer = 12, fraction = 4, message = "目的国申报价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal toDeclarePrice;

        /**
        * 目的国申报币种
        */
//        @NotBlank(message = "目的国申报币种不能为空")
        @Size(max = 32,message = "目的国申报币种最大长度不能超过32位")
        private String toCurrency;

        /**
        * 目的国申报币种符号
        */
//        @NotBlank(message = "目的国申报币种符号不能为空")
        @Size(max = 255,message = "目的国申报币种符号最大长度不能超过255位")
        private String toCurrencySymbol;

        /**
        * 目的国申报类型（dictType=toDeclarePriceType）
        */
//        @NotBlank(message = "目的国申报类型（dictType=toDeclarePriceType）不能为空")
        @Size(max = 30,message = "目的国申报类型最大长度不能超过30位")
        private String toDeclarePriceType;

        /**
        * 固定比例（当toDeclarePriceType=ratePrice）必填
        */
//        @NotNull(message = "固定比例（当toDeclarePriceType=ratePrice）必填不能为空")
        @Digits(integer = 10, fraction = 2, message = "固定比例必填整数位不能超过10位，小数位不能超过2位")
        private BigDecimal rate;

        /**
        * 最高申报价
        */
//        @NotNull(message = "最高申报价不能为空")
        @Digits(integer = 12, fraction = 4, message = "最高申报价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal maxDeclarePrice;

        /**
        * 最低申报价
        */
//        @NotNull(message = "最低申报价不能为空")
        @Digits(integer = 12, fraction = 4, message = "最低申报价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal minDeclarePrice;


    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 规则名称
         */
        private String name;
    }

    /**
     * 分页详情
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {


        private String id;
        /**
         * 名称
         */
        private String name;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 禁用状态
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人
         */
        private String createUserName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人
         */
        private String updateUserName;


        /**
         * 修改时间
         */
        private LocalDateTime updateTime;


    }
}