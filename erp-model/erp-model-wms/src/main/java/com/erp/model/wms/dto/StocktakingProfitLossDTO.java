package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname StocktakingProfitLossDTO
 * @Description TODO
 * @Date 2023-08-03 18:07
 * @Created by yl
 */
public class StocktakingProfitLossDTO  implements Serializable {


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
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {
        /**
         * 单号
         */
        private String code;

        /**
         * 来源单号 盘点任务单号
         */
        private String sourceCode;

        /**
         * 单据状态集合
         */
        private List<String> approveStatusList;


        /**
         * 单据类型
         */
        private List<String> billTypeList;








        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 创建人id
         */
        private List<LocalDate> createUserId;

        /**
         * 仓库
         */
        private String warehouseId;

        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 标识
         */
        private String tabFlag;

    }
}
