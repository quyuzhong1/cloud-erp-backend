package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 文件模板url表请求响应实体
 * </p>
 *
 * @author wangwei
 * @since 2023-12-25
*/
@Data
@NoArgsConstructor
public class FileTemplateDTO implements Serializable {




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
        * 来源类型
        */
        private String sourceType;

        /**
        * 业务名称
        */
        private String name;

        /**
        * 文件路径url
        */
        private String url;

        /**
        * 文件类型
        */
        private String fileType;


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
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 业务名称
        */
        @NotBlank(message = "业务名称不能为空")
        @Size(max = 100,message = "业务名称最大长度不能超过100位")
        private String name;

        /**
        * 文件路径url
        */
        @NotBlank(message = "文件路径url不能为空")
        @Size(max = 255,message = "文件路径url最大长度不能超过255位")
        private String url;

        /**
        * 文件类型
        */
        @NotBlank(message = "文件类型不能为空")
        @Size(max = 10,message = "文件类型最大长度不能超过10位")
        private String fileType;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetOneDTO {

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
         * 业务名称
         */
        @NotBlank(message = "业务名称不能为空")
        @Size(max = 100,message = "业务名称最大长度不能超过100位")
        private String name;

        /**
         * 文件类型
         */
        @NotBlank(message = "文件类型不能为空")
        @Size(max = 10,message = "文件类型最大长度不能超过10位")
        private String fileType;
    }


    @Data
    @NoArgsConstructor
    public static class FastdfsAddOrUpdateDTO {

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
         * 业务名称
         */
        @NotBlank(message = "业务名称不能为空")
        @Size(max = 100,message = "业务名称最大长度不能超过100位")
        private String name;

        /**
         * 文件类型
         */
        @NotBlank(message = "文件类型不能为空")
        @Size(max = 10,message = "文件类型最大长度不能超过10位")
        private String fileType;

        /**
         * 文件不能为空
         */
        @NotBlank(message = "文件不能为空")
        private MultipartFile file;
    }
}