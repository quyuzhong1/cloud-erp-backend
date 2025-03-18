package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ExcelImportFsDTO {



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlDTO {
        /**
         * 正确Url
         */
        private String successUrl;
        /**
         * 错误Url
         */
        private String errorUrl;
    }
}
