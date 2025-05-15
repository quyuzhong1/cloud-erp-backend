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
 * 三方审批定义请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ThirdProcessDefinitionDTO implements Serializable {




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
        * 单据编码
        */
        private String approvalCode;

        /**
        * 状态
        */
        private Boolean status;

        /**
        * 单据名称
        */
        private String name;

        /**
        * 归属平台
        */
        private String sourcePlatform;

        /**
        * 表单json
        */
        private String formJson;

        /**
        * 审批组
        */
        private String dictApprovalGroup;

        /**
        * 审批定义类型：发起/拉取
        */
        private String type;


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
        * 单据编码
        */
        @NotBlank(message = "单据编码不能为空")
        @Size(max = 30,message = "单据编码最大长度不能超过30位")
        private String approvalCode;

        /**
        * 状态
        */
        @NotBlank(message = "状态不能为空")
        private Boolean status;

        /**
        * 单据名称
        */
        @NotBlank(message = "单据名称不能为空")
        @Size(max = 30,message = "单据名称最大长度不能超过30位")
        private String name;

        /**
        * 归属平台
        */
        @NotBlank(message = "归属平台不能为空")
        @Size(max = 30,message = "归属平台最大长度不能超过30位")
        private String sourcePlatform;

        /**
        * 表单json
        */
        @NotBlank(message = "表单json不能为空")
        @Size(max = 30,message = "表单json最大长度不能超过30位")
        private String formJson;

        /**
        * 审批组
        */
        @NotBlank(message = "审批组不能为空")
        @Size(max = 30,message = "审批组最大长度不能超过30位")
        private String dictApprovalGroup;

        /**
        * 审批定义类型：发起/拉取
        */
        @NotBlank(message = "审批定义类型：发起/拉取不能为空")
        @Size(max = 30,message = "审批定义类型：发起/拉取最大长度不能超过30位")
        private String type;


    }

    @Data
    @NoArgsConstructor
    public static class DropDownDTO {
        /**
         * 单据编码
         */
        private String code;

        /**
         * 单据名称
         */
        private String name;
    }
}