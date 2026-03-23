package com.erp.model.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * @date 2024-09-05
 * @author tanmujin
 */
@Data
public class SysCommonDTO implements Serializable {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttachmentDTO{
        /**
         * 文件名称
         */
        private String attachName;
        /**
         * 文件url
         */
        private String attachUrl;
        /**
         * 文件大小 MB
         */
        private BigDecimal attachSize;
    }
}
