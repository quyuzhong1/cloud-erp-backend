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
 * 三方流程实例拉取任务请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ThirdProcessPullPlanDTO implements Serializable {




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
        * 审批创建时间
        */
        private String startTime;

        /**
        * 审批完成时间
        */
        private String endTime;

        /**
        * 状态
        */
        private String status;

        /**
        * 审批定义 Code
        */
        private String approvalCode;


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
        * 状态
        */
        @NotBlank(message = "状态不能为空")
        @Size(max = 10,message = "状态最大长度不能超过10位")
        private String status;

        /**
        * 审批定义 Code
        */
        @NotBlank(message = "审批定义 Code不能为空")
        @Size(max = 30,message = "审批定义 Code最大长度不能超过30位")
        private String approvalCode;


    }


}