package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @date 2023/8/3 12:17
 */
@Data
@NoArgsConstructor
public class BaseDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QtyDTO {

        /**
         * id
         */
        private String id;

        /**
         * 数量
         */
        private Integer qty;

    }
}
