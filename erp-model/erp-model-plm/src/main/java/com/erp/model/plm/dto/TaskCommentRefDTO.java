package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 任务评论关联表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@NoArgsConstructor
public class TaskCommentRefDTO implements Serializable {




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
        * 任务评论表id
        */
        private String taskCommentId;
        /**
        * 关联人员id
        */
        private String refUserId;
        /**
        * 发送通知结果true 成功
        */
        private Boolean sendNoticeResult;
        /**
        * 任务id
        */
        private String taskId;

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
        * 任务评论表id
        */
        @NotBlank(message = "任务评论表id不能为空")
        @Size(max = 19,message = "任务评论表id最大长度不能超过19位")
        private String taskCommentId;
        /**
        * 关联人员id
        */
        @NotBlank(message = "关联人员id不能为空")
        @Size(max = 19,message = "关联人员id最大长度不能超过19位")
        private String refUserId;
        /**
        * 发送通知结果true 成功
        */
        @NotNull(message = "发送通知结果true 成功不能为空")
        private Boolean sendNoticeResult;
        /**
        * 任务id
        */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 19,message = "任务id最大长度不能超过19位")
        private String taskId;

    }


}