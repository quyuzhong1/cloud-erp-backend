package com.erp.model.srm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 公共附件表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-20
*/
@Data
@NoArgsConstructor
public class AttachmentDTO implements Serializable {


    /**
     * 删除的
     */
    @Data
    @NoArgsConstructor
    public  static class DeleteDTO {

        /**
         * 业务表id
         */
        private String businessId;


        /**
         * 资质附件url
         */
        @NotBlank(message = "附件地址不能为空")
        private String attachUrl;


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
        private String  id;

        /**
        * 业务表id
        */
        private String businessId;

        /**
        * 类型 存表名
        */
        private String type;

        /**
        * 附件的文件地址
        */
        private String attachUrl;

        /**
        * 附件的文档的名称
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
    public static class CommonDTO {

        /**
        * 业务表id
        */
        @NotBlank(message = "业务表id不能为空")
        @Size(max = 19,message = "业务表id最大长度不能超过19位")
        private String businessId;

        /**
        * 类型 存表名
        */
        @NotBlank(message = "类型 存表名不能为空")
        @Size(max = 50,message = "类型 存表名最大长度不能超过50位")
        private String type;

        /**
        * 附件的文件地址
        */
        @NotBlank(message = "附件的文件地址不能为空")
        @Size(max = 200,message = "附件的文件地址最大长度不能超过200位")
        private String attachUrl;

        /**
        * 附件的文档的名称
        */
        @NotBlank(message = "附件的文档的名称不能为空")
        @Size(max = 100,message = "附件的文档的名称最大长度不能超过100位")
        private String attachName;


    }


}