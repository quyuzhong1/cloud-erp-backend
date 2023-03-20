package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname AttachmentDTO
 * @Description TODO
 * @Date 2023-03-20 9:56
 * @Created by yl
 */
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
         * 业务表id
         */
        private String businessId;


    }
}
