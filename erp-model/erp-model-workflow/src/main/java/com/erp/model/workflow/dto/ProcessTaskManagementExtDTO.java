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
 * process_task_management拓展表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ProcessTaskManagementExtDTO implements Serializable {




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
        * process_task_management_id
        */
        private String processTaskManagementId;

        /**
        * message_id
        */
        private String messageId;

        /**
        * 来源平台
        */
        private String soucePlatform;


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
        * process_task_management_id
        */
        @NotBlank(message = "process_task_management_id不能为空")
        @Size(max = 19,message = "process_task_management_id最大长度不能超过19位")
        private String processTaskManagementId;

        /**
        * message_id
        */
        @NotBlank(message = "message_id不能为空")
        @Size(max = 64,message = "message_id最大长度不能超过64位")
        private String messageId;

        /**
        * 来源平台
        */
        @NotBlank(message = "来源平台不能为空")
        @Size(max = 50,message = "来源平台最大长度不能超过50位")
        private String soucePlatform;


    }


    @Data
    @NoArgsConstructor
    public static class MessageDTO {


        /**
         * message_id
         */

        private String messageId;

        /**
         * curApproveId
         */
        private String curApproveId;

        /**
         * curApproveName
         */
        private String curApproveName;


    }


}