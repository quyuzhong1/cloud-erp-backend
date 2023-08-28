package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String name;

        /**
        * 优先级
        */
        @NotBlank(message = "优先级不能为空")
        @Size(max = 5,message = "优先级最大长度不能超过5位")
        private String priority;

        /**
        * 是否禁用 false 未禁用
        */
        @NotNull(message = "是否禁用 false 未禁用不能为空")
        private Boolean disabled;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 分类明细id
        */
        @NotBlank(message = "分类明细id不能为空")
        @Size(max = 19,message = "分类明细id最大长度不能超过19位")
        private String categoryDetailId;

        /**
        * 操作类型多个逗号分割
        */
        @NotBlank(message = "操作类型多个逗号分割不能为空")
        @Size(max = 30,message = "操作类型多个逗号分割最大长度不能超过30位")
        private String operationType;

        /**
        * 流向状态
        */
        @NotBlank(message = "流向状态不能为空")
        @Size(max = 30,message = "流向状态最大长度不能超过30位")
        private String flowStatus;


    }


}