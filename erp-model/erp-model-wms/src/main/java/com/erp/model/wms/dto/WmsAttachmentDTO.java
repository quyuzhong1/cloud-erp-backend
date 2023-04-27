package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname WmsAttachmentDTO
 * @Description TODO
 * @Date 2023-04-19 9:57
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WmsAttachmentDTO  implements Serializable {



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
