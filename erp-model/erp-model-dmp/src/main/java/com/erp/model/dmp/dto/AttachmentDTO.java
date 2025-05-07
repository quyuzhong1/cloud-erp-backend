package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 附件表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-06
*/
@Data
@NoArgsConstructor
public class AttachmentDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public  static class UpdateDTO {

        /**
         * 主表id
         */
        private String id;

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