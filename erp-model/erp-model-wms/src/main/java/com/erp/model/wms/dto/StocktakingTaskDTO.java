package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 盘点任务
 *
 * @author Lambda
 * @Classname StocktakingDTO
 * @Description TODO
 * @Date 2023-07-31 14:29
 * @Created by yl
 */
public class StocktakingTaskDTO implements Serializable {

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabDTO {
        /**
         * 标识
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;

    }


    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {
        /**
         * 标识
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;

    }
}
