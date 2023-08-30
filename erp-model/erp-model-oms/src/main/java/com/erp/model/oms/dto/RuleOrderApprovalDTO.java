package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;

/**
 * <p>
 * 订单审核规则请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class RuleOrderApprovalDTO implements Serializable {


    /**
     * 分页详情
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

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

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 规则名称
         */
        private String name;
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
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 优先级
         */
        private String priority;

        /**
         * 是否禁用 false 未禁用
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

        /**
         * 分类明细id
         */
        private String categoryDetailId;

        /**
         * 操作类型多个逗号分割
         */
        private String operationType;

        /**
         * 流向状态
         */
        private String flowStatus;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 名称
         */
        @NotBlank(message = "规则名称不能为空")
        @Size(max = 50, message = "规则名称最大长度不能超过50字符")
        private String name;

        /**
         * 优先级
         */
        @NotNull(message = "优先级不能为空")
        @DecimalMin(value = "0",message ="最小值为1" )
        @DecimalMax(value = "10",message ="最小值为10" )
        private Integer priority;



        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 分类明细id
         */
        private String categoryDetailId;

        /**
         * 操作类型多个逗号分割
         * category 分类
         * flowStatus 流向状态
         */
        @NotBlank(message = "操作类型多个逗号分割不能为空")
        private String operationType;

        /**
         * 流向状态
         */
        private String flowStatus;

        private List<RuleConditionDTO.AddDTO> conditionList;
    }


}