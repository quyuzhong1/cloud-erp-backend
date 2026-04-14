package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Lambda
 * @Classname WmsAttachmentDTO

 * @Date 2023-04-19 9:57
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WmsAttachmentDTO  implements Serializable {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddDTO {

        private String fileName;

        private String url;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddVersionDTO {
        /**
         * 业务id
         */
        private String businessId;
        /**
         * 附件url
         */
        private String attachUrl;
        /**
         * 附件名称
         */
        private String attachName;
        /**
         * 附件类型
         */
        private String type;
        /**
         * 附件版本
         */
        private Integer attachVersion;
        /**
         * 附件大小
         */
        private BigDecimal attachSize;

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
         * 业务类型
         */
        private String businessType;

        /**
         * 资质附件url
         */
        @NotBlank(message = "附件地址不能为空")
        private String attachUrl;


    }

    /**
     * 删附件信息
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 主表id
         */
        private String id;


        /**
         * 类型
         */
        private String type;

        /**
         * 资质附件url
         */
        private String attachUrl;

        /**
         * 资质附件名称
         */
        private String attachName;


        /**
         * 业务表id
         */
        private String businessId;
    }
}
