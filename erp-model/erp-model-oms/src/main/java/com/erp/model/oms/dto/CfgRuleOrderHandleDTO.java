package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
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
 * 订单处理规则表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-05-09
*/
@Data
@NoArgsConstructor
public class CfgRuleOrderHandleDTO implements Serializable {

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id【可排序】
         */
        private String id;

        /**
         * 名称【可排序】
         */
        private String name;

        /**
         * 优先级【可排序】
         */
        private Integer priority;

        /**
         * 禁用状态【可排序】
         */
        private Boolean disabled;

        /**
         * 创建人【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 修改人【可排序】
         */
        private String updateUserName;

        /**
         * 修改时间【可排序】
         */
        private LocalDateTime updateTime;

    }

    /**
     * 分页列表查询参数
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
        * 规则描述
        */
        private String remark;

        /**
        * 优先级
        */
        private Integer priority;

        /**
        * 城市（推送物流商下单为空）
        */
        private Boolean isPushCity;

        /**
        * 州（推送物流商下单为空）
        */
        private Boolean isPushProvince;

        /**
         * 规则条件
         */
        private List<RuleConditionDTO.ViewDTO> conditionList;
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

        /**
         * 规则条件
         */
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
        * 规则描述
        */
        @Size(max = 255,message = "规则描述最大长度不能超过255位")
        private String remark;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        /**
        * 城市（推送物流商下单为空）
        */
        private Boolean isPushCity;

        /**
        * 州（推送物流商下单为空）
        */
        private Boolean isPushProvince;

    }


}