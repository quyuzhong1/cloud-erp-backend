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
 * 任务关注的人请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-20
*/
@Data
@NoArgsConstructor
public class TemplateTaskConcernDTO implements Serializable {




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
        * 产品id
        */
        private String productId;

        /**
        * 任务id
        */
        private String taskId;

        /**
        * 关注的人
        */
        private String userId;


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
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String productId;

        /**
        * 任务id
        */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 19,message = "任务id最大长度不能超过19位")
        private String taskId;

        /**
        * 关注的人
        */
        @NotBlank(message = "关注的人不能为空")
        @Size(max = 19,message = "关注的人最大长度不能超过19位")
        private String userId;


    }


}