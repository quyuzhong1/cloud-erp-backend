package com.erp.model.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

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
    }
}
