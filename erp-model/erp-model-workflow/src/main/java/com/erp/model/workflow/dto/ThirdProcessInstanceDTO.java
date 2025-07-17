package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 三方流程实例清单请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ThirdProcessInstanceDTO implements Serializable {




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
        * 审批名称
        */
        private String approvalName;

        /**
        * 审批创建时间
        */
        private String startTime;

        /**
        * 审批完成时间
        */
        private String endTime;

        /**
        * 审批单编号
        */
        private String serialNumber;

        /**
        * 状态
        */
        private String status;

        /**
        * 审批表单控件 JSON 字符串
        */
        private String form;

        /**
        * 审批定义 Code
        */
        private String approvalCode;

        /**
        * 审批实例 Code
        */
        private String instanceCode;

        /**
         * 是否转换完成
         */
        private Boolean isComplete;

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
        * 审批名称
        */
        @NotBlank(message = "审批名称不能为空")
        @Size(max = 30,message = "审批名称最大长度不能超过30位")
        private String approvalName;

        /**
        * 审批创建时间
        */
        @NotBlank(message = "审批创建时间不能为空")
        @Size(max = 50,message = "审批创建时间最大长度不能超过50位")
        private String startTime;

        /**
        * 审批完成时间
        */
        @NotBlank(message = "审批完成时间不能为空")
        @Size(max = 50,message = "审批完成时间最大长度不能超过50位")
        private String endTime;

        /**
        * 审批单编号
        */
        @NotBlank(message = "审批单编号不能为空")
        @Size(max = 30,message = "审批单编号最大长度不能超过30位")
        private String serialNumber;

        /**
        * 状态
        */
        @NotBlank(message = "状态不能为空")
        @Size(max = 10,message = "状态最大长度不能超过10位")
        private String status;

        /**
        * 审批表单控件 JSON 字符串
        */
        @NotBlank(message = "审批表单控件 JSON 字符串不能为空")
        @Size(max = 255,message = "审批表单控件 JSON 字符串最大长度不能超过255位")
        private String form;

        /**
        * 审批定义 Code
        */
        @NotBlank(message = "审批定义 Code不能为空")
        @Size(max = 30,message = "审批定义 Code最大长度不能超过30位")
        private String approvalCode;

        /**
        * 审批实例 Code
        */
        @NotBlank(message = "审批实例 Code不能为空")
        @Size(max = 30,message = "审批实例 Code最大长度不能超过30位")
        private String instanceCode;

        /**
         * 节点集合
         */
        @NotBlank(message = "节点集合不能为空")
        private String taskList;
    }


}