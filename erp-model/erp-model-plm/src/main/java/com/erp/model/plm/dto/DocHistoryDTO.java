package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname DocHistoryDTO
 * @Description TODO
 * @Date 2023-06-25 9:44
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DocHistoryDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class InfoDTO {

        /**
         * 交付文档表id
         */
        private String requireDocId;

        /**
         * 交付文档表名
         */
        private String requireDocName;

        /**
         * 文档名称
         */
        private String fileName;

        /**
         * 文档名称
         */
        private String fileUrl;

        /**
         * 提交人
         */
        private String createUserName;


        /**
         * 提交时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 版本
         */
        private Integer changeVersion;

    }
}
