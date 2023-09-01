package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流规则表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class RuleLogisticsDTO implements Serializable {


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
         * 禁用状态 false 未禁用
         */
        private Boolean disabled;

        /**
         * 备注描述
         */
        private String remark;

        /**
         * 物流方式类型
         */
        private String modeType;

        /**
         * 物流方式类型名
         */
        private String modeTypeName;

        /**
         * 物流供应商
         */
        private String logisticsSupplier;

        /**
         * 物流方式
         */
        private String mode;

        /**
         * 是否自动获取物流单号
         */
        private Boolean autoGetTrackNo;

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
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50, message = "名称最大长度不能超过50位")
        private String name;

        /**
         * 优先级
         */
        @NotBlank(message = "优先级不能为空")
        @Size(max = 5, message = "优先级最大长度不能超过5位")
        private String priority;

        /**
         * 禁用状态 false 未禁用
         */
        @NotNull(message = "禁用状态 false 未禁用不能为空")
        private Boolean disabled;

        /**
         * 备注描述
         */
        @NotBlank(message = "备注描述不能为空")
        @Size(max = 255, message = "备注描述最大长度不能超过255位")
        private String remark;

        /**
         * 物流方式类型
         */
        @NotBlank(message = "类型不能为空")
        @Size(max = 50, message = "类型多个逗号分割最大长度不能超过50位")
        private String moduleType;

        /**
         * 物流供应商
         */
        @NotBlank(message = "物流供应商不能为空")
        @Size(max = 255, message = "物流供应商最大长度不能超过255位")
        private String logisticsSupplier;

        /**
         * 物流方式
         */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 50, message = "物流方式最大长度不能超过50位")
        private String mode;

        /**
         * 是否自动获取物流单号
         */
        @NotNull(message = "是否自动获取物流单号 不能为空")
        private Boolean autoGetTrackNo;


    }


}