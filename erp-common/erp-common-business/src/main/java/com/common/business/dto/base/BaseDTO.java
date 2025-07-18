package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @date 2023/8/3 12:17
 */
@Data
public class BaseDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QtyDTO implements Serializable {

        /**
         * id
         */
        private String id;

        /**
         * 数量
         */
        private Integer qty;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportDTO implements Serializable {
        /**
         * 文件URL
         */
        private String fileUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportResultDTO implements Serializable {
        /**
         * 失败文件URL
         */
        private String errorUrl;
        /**
         * 数据总条数
         */
        private Integer count;
    }
}
