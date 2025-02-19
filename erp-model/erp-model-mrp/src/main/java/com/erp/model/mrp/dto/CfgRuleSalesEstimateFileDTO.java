package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CfgRuleSalesEstimateFileDTO {

    @Getter
    @Setter
    public static class PagingView {
        /**
         * 文件名
         */
        private String fileName;
        /**
         * 文件地址
         */
        private String fileUrl;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Getter
    @Setter
    public static class PagingParamDTO {

    }

    @Getter
    @Setter
    public static class ExcelDTO {

    }
}
