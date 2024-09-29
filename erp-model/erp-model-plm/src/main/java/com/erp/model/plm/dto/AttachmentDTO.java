package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 附件表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@NoArgsConstructor
public class AttachmentDTO implements Serializable {




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
        * 类型 默认表名
        */
        private String type;
        /**
        * 附件地址
        */
        private String attachUrl;
        /**
        * 附件名称
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
        * 类型 默认表名
        */
        private String type;
        /**
        * 附件地址
        */
        @NotBlank(message = "附件地址不能为空")
        @Size(max = 100,message = "附件地址最大长度不能超过100位")
        private String attachUrl;
        /**
        * 附件名称
        */
        @NotBlank(message = "附件名称不能为空")
        @Size(max = 50,message = "附件名称最大长度不能超过50位")
        private String attachName;

    }


    /**
     * 列表数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 附件名称
         */
        private String attachName;

        /**
         * 附件URL
         */
        private String attachUrl;

        /**
         * 上传日期
         */
        private LocalDateTime createTime;

        /**
         * 大小
         */
        private BigDecimal attachSize;

    }


    @Data
    @NoArgsConstructor
    public static class BatchUploadDTO {

        /**
         * 类型
         */
        @NotBlank(message = "类型不能为空")
        private String type;

        /**
         * 文件
         */
        @NotEmpty(message = "文件不能为空")
        private List<MultipartFile> multipartFileList;

    }

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
}