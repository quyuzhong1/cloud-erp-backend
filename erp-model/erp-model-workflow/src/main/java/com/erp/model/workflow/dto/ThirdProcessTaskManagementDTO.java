package com.erp.model.workflow.dto;

import java.time.LocalDateTime;
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
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-05-23
*/
@Data
@NoArgsConstructor
public class ThirdProcessTaskManagementDTO implements Serializable {




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
        * 节点id
        */
        private String nodeId;

        /**
        * 任务id
        */
        private String taskId;

        /**
        * third_process_management_id
        */
        private String mainId;

        /**
        * 节点名称
        */
        private String nodeName;

        /**
        * 任务状态
        */
        private String taskStatus;

        /**
        * 系统用户id
        */
        private String sysUserId;

        /**
        * 三方平台审批人的 user_id
        */
        private String thirdUserId;

        /**
        * 任务开始时间
        */
        private LocalDateTime startTime;

        /**
        * 任务结束时间
        */
        private LocalDateTime endTime;


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
        * 节点id
        */
        @NotBlank(message = "节点id不能为空")
        @Size(max = 64,message = "节点id最大长度不能超过64位")
        private String nodeId;

        /**
        * 任务id
        */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 64,message = "任务id最大长度不能超过64位")
        private String taskId;

        /**
        * third_process_management_id
        */
        @NotBlank(message = "third_process_management_id不能为空")
        @Size(max = 32,message = "third_process_management_id最大长度不能超过32位")
        private String mainId;

        /**
        * 节点名称
        */
        @NotBlank(message = "节点名称不能为空")
        @Size(max = 32,message = "节点名称最大长度不能超过32位")
        private String nodeName;

        /**
        * 任务状态
        */
        @NotBlank(message = "任务状态不能为空")
        @Size(max = 32,message = "任务状态最大长度不能超过32位")
        private String taskStatus;

        /**
        * 系统用户id
        */
        @NotBlank(message = "系统用户id不能为空")
        @Size(max = 100,message = "系统用户id最大长度不能超过100位")
        private String sysUserId;

        /**
        * 三方平台审批人的 user_id
        */
        @NotBlank(message = "三方平台审批人的 user_id不能为空")
        @Size(max = 64,message = "三方平台审批人的 user_id最大长度不能超过64位")
        private String thirdUserId;

        /**
        * 任务开始时间
        */
        private LocalDateTime startTime;

        /**
        * 任务结束时间
        */
        private LocalDateTime endTime;


    }


}