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
 * 审核附件表请求响应实体
 * </p>
 *
 * @author tmj
 * @since 2024-09-05
*/
@Data
@NoArgsConstructor
public class ProcessTaskManagementAttachmentDTO implements Serializable {




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
        * 文件url
        */
        private String attachUrl;

        /**
        * 文件名称
        */
        private String attachName;


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
    @AllArgsConstructor
    public static class CommonDTO {

        /**
        * 文件url
        */
        private String attachUrl;

        /**
        * 文件名称
        */
        private String attachName;
    }


}