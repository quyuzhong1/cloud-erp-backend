package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcProductDTO
 * @Description TODO
 * @Date 2023-04-14 15:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcProductDTO {


    /**
     * 暂存 质检产品信息
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * sku
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;


        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        /**
         * 箱长
         */
        private BigDecimal boxLength;

        /**
         * 箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 箱高
         */
        private BigDecimal boxHeight;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * 外箱图片地址集合
         */
        private List<String> boxImageUrlList;

        /**
         * 外箱图片名称集合
         */
        private List<String> boxImageNameList;


        /**
         * 产品图片地址集合
         */
        private List<String> productImageUrlList;

        /**
         * 产品名称地址集合
         */
        private List<String> productImageNameList;



    }
}
