package com.erp.model.fms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * FMS附件DTO
 * </p>
 *
 * @author wuht
 * @since 2025-10-16
 */
@Data
@NoArgsConstructor
public class AttachmentDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddDTO {

        private String fileName;

        private String url;

    }

    /**
     * 删除的
     */
    @Data
    @NoArgsConstructor
    public static class DeleteDTO {

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
     * 查询附件信息
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

