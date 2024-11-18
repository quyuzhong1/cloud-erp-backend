package com.erp.model.wms.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 波次规则请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-06-20
*/
@Data
@NoArgsConstructor
public class CfgRuleWaveDTO implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    /**
     * 列表参数
     */
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

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id【可排序】
         */
        private String id;
        /**
         * 优先级【可排序】
         */
        private String priority;
        /**
         * 名称【可排序】
         */
        private String name;
        /**
         * 波次类型【可排序】
         */
        @Dict(queryTypeField = "waveType")
        private String waveType;
        /**
         * 拣货车类型【可排序】
         */
        private String pickingCartTypeJson;
        /**
         * 拣货车类型
         */
        private String pickingCartTypeName;
        /**
         * 最小单数【可排序】
         */
        private String minOrderQty;
        /**
         * 最大单数【可排序】
         */
        private String maxOrderQty;
        /**
         * 最少商品数量【可排序】
         */
        private String minQty;
        /**
         * 最大商品数量【可排序】
         */
        private String maxQty;
        /**
         * 状态【可排序】
         */
        private Boolean disabled;
        /**
         * 更新时间【可排序】
         */
        private String updateTime;
        /**
         * 更新人【可排序】
         */
        private String updateUserName;

    }


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
        * 名称
        */
        private String name;

        /**
        * 波次类型((waveType类型)
        */
        @Dict(queryTypeField = "waveType")
        private String waveType;

        /**
        * 优先级
        */
        private Integer priority;

        /**
        * 拣货车类型id
        */
        private String pickingCartTypeJson;

        /**
         * 拣货车类型id集合
         */
        private List<String> pickingCartTypeIdList;

        /**
        * 最小单数
        */
        private Integer minOrderQty;

        /**
        * 最大单数
        */
        private Integer maxOrderQty;

        /**
        * 最少商品数量
        */
        private Integer minQty;

        /**
        * 最多商品数量
        */
        private Integer maxQty;

        /**
        * 状态,true禁用，false启用
        */
        private Boolean disabled;

        /**
        * 执行时间
        */
        @JsonFormat(pattern = "HH:mm")
        private List<LocalTime> executionTimeList;

        /**
        * 执行类型（auto自动执行，manual手动执行）
        */
        private String executionType;

        /**
        * 分拣方式（sameWave边拣边分，mixedWave先拣后分）
        */
        private String pickingType;

        /**
        * 规则描述
        */
        private String remark;

        /**
         * 规则条件
         */
        @Dict
        private List<CfgRuleConditionDTO.View> conditionList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 规则条件
         */
        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Add> conditionList;

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

        /**
         * 规则条件
         */
        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Update> conditionList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;

        /**
        * 波次类型((waveType类型)
        */
        @NotBlank(message = "波次类型((waveType类型)不能为空")
        @Size(max = 32,message = "波次类型((waveType类型)最大长度不能超过32位")
        private String waveType;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        /**
        * 拣货车类型id集合
        */
        @NotEmpty(message = "拣货车类型id不能为空")
        private List<String> pickingCartTypeIdList;

        /**
        * 最小单数
        */
        @NotNull(message = "最小单数不能为空")
        @Min(value = 1,message = "实发数量最小值为1")
        @Max(value = 999999999,message = "实发数量最大值为999999999")
        private Integer minOrderQty;

        /**
        * 最大单数
        */
        @NotNull(message = "最大单数不能为空")
        @Min(value = 1,message = "实发数量最小值为1")
        @Max(value = 999999999,message = "实发数量最大值为999999999")
        private Integer maxOrderQty;

        /**
        * 最少商品数量
        */
        @Min(value = 1,message = "实发数量最小值为1")
        @Max(value = 999999999,message = "实发数量最大值为999999999")
        private Integer minQty;

        /**
        * 最多商品数量
        */
        @Min(value = 1,message = "实发数量最小值为1")
        @Max(value = 999999999,message = "实发数量最大值为999999999")
        private Integer maxQty;

        /**
        * 状态,true禁用，false启用
        */
        @NotNull(message = "状态,true禁用，false启用不能为空")
        private Boolean disabled;

        /**
        * 执行类型（自动执行，手动执行）
        */
        @NotBlank(message = "执行类型（自动执行，手动执行）不能为空")
        @Size(max = 32,message = "执行类型（自动执行，手动执行）最大长度不能超过32位")
        private String executionType;

        /**
         * 自动执行时间
         */
        @JsonFormat(pattern = "HH:mm")
        private List<LocalTime> executionTimeList;

        /**
        * 分拣方式（边拣边分，先拣后分）
        */
        @NotBlank(message = "分拣方式（边拣边分，先拣后分）不能为空")
        @Size(max = 32,message = "分拣方式（边拣边分，先拣后分）最大长度不能超过32位")
        private String pickingType;

        /**
        * 规则描述
        */
        @Size(max = 200,message = "规则描述最大长度不能超过200位")
        private String remark;


    }

    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {
        /**
         * 主键id
         */
        @NotEmpty(message = "主键id不能为空")
        private List<String> idList;

        /**
         * 是否禁用，true是,false否
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

    }

}