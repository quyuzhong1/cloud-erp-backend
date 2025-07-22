package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * sku对照表匹配规则请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2023-12-21
 */
@Data
@NoArgsConstructor
public class SkuMappingRuleDTO implements Serializable {


    /**
     * 列表DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {
        /**
         * id
         */
        private String id;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 规则名称
         */
        private String ruleName;

        /**
         * 规则类型
         */
        private String ruleType;

        /**
         * 规则类型
         */
        private String ruleTypeName;

        /**
         * 状态
         */
        private Boolean disabled;

        /**
         * 操作人
         */
        private String updateUserName;

        /**
         * 操作时间
         */
        private LocalDateTime updateTime;
    }

    /**
     * 详情DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamsDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }
    /**
     * 详情DTO
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewDTO extends CommonDTO{
        /**
         * id
         */
        private String id;

        /**
         * 规则类型名称
         */
        private String ruleTypeName;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    /**
     * 规则条件
     */
    @Data
    @NoArgsConstructor
    public static class RuleConditionsDTO  {

        /**
         * 忽略前缀
         */
        private String ignorePrefix;

        /**
         * 忽略后缀
         */
        private String ignoringSuffixes;

        /**
         * 忽略前..位
         */
        private Integer ignoringBeforePosition;

        /**
         * 忽略后..位
         */
        private Integer ignoringAfterPosition;

        /**
         * 截取第。。位
         */
        private Integer interceptionFrontPosition;

        /**
         * 至截取后。。位
         */
        private Integer interceptionBehindPosition;

        /**
         * 起始符 url: common/enumDropDown?type=SkuMappingSymbolic
         */
        private String startingSymbol;

        /**
         * 结束符 url: common/enumDropDown?type=SkuMappingSymbolic
         */
        private String endSymbol;

        /**
         * 起始符有效位置 url: common/enumDropDown?type=SkuMappingSymbolicSide
         */
        private String validStartingSymbolPosition;

        /**
         * 结束符有效位置 url: common/enumDropDown?type=SkuMappingSymbolicSide
         */
        private String validEndSymbolPosition;

        /**
         * 子件组合拆分符号
         */
        private String childCombineSplitSymbol;

        /**
         * 子件数量拆分符号
         */
        private String childQtySplitSymbol;

        /**
         * 无需匹配勾选项集合 平台父产品parent 停售 inactive  删除  delete 草稿incomplete
         */
        private List<String> noMatchList;
    }
    /**
     * 扩展规则条件
     */
    @Data
    @NoArgsConstructor
    public static class ExtendRuleConditionsDTO  {

        /**
         * 替换前字符
         */
        private String beforeReplacingCharacters = "";

        /**
         * 替换后字符
         */
        private String afterReplacingCharacters = "";
    }

    /**
     * 新增
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

    }

    /**
     * 规则测试dto
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class RuleTestDTO extends CommonDTO {

        /**
         * SKU
         */
        @NotBlank(message = "sku不能为空")
        private String skuNo;
    }
    /**
     * 修改
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    /**
     * 状态DTO
     */
    @Data
    @NoArgsConstructor
    public static class StatusDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;


        /**
         * 是否禁用
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 优先级:1-5
         */
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        /**
         * 规则类型 url: common/enumDropDown?type=SkuMappingRule
         */
        @NotBlank(message = "规则类型不能为空")
        @Size(max = 50, message = "规则类型最大长度不能超过50位")
        private String ruleType;

        /**
         * 规则名称
         */
        private String ruleName;

        /**
         * 规则正则
         */
        @JsonIgnore
        private String ruleRegex;

        /**
         * 扩展规则
         */
        private String extendRuleType;

        /**
         * 扩展规则正则
         */
        @JsonIgnore
        private String extendRuleRegex;

        /**
         * 规则DTO{@link com.erp.model.oms.enums.SkuMappingRuleEnum}
         */
        private RuleDTO ruleDTO;

        /**
         * 扩展规则DTO{@link com.erp.model.oms.enums.SkuMappingRuleEnum.SkuMappingExtendRuleEnum}
         */
        private ExtendRuleDTO extendRuleDTO;
    }


    @Data
    @NoArgsConstructor
    public static class RuleDTO  {

        /**
         * 规则详情
         */
        List<RuleConditionsDTO> ruleContentList;
    }
    /**
     * 扩展规则条件
     */
    @Data
    @NoArgsConstructor
    public static class ExtendRuleDTO  {

        /**
         * 扩展规则详情
         */
        List<ExtendRuleConditionsDTO> extendRuleContentList;
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitSkuDTO {

        private String sku;
        private Integer qty;

        public String desc(){
            return sku + "数量" + qty;
        }
        public String matchStr(){
            return sku +"*"  + qty +"丨";
        }
    }

    @Data
    @NoArgsConstructor
    public static class LogDTO {

        /**
         * 优先级:1-5
         */
        private Integer priority;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 规则类型 url: common/enumDropDown?type=SkuMappingRule
         */
        private String ruleTypeName;

        /**
         * 扩展规则
         */
        private String extendRuleType;

        /**
         * 起始符有效位置 url: common/enumDropDown?type=SkuMappingSymbolicSide
         */
        private String validStartingSymbolPosition;

        /**
         * 结束符有效位置 url: common/enumDropDown?type=SkuMappingSymbolicSide
         */
        private String validEndSymbolPosition;

        /**
         * 规则详情
         */
        private List<RuleConditionsDTO> ruleContentList;

        /**
         * 扩展规则详情
         */
        private List<ExtendRuleConditionsDTO> extendRuleContentList;
    }

}