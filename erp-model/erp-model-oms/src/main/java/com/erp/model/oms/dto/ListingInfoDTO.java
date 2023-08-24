package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname ListingInfoDTO
 * @Description TODO
 * @Date 2023-08-18 16:13
 * @Created by yl
 */
public class ListingInfoDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable{
        /**
         * id
         */
        private String id;

        /**
         * 平台sku no
         */
        private String platformSkuNo;


        /**
         * 平台产品名
         */
        private String platformProductName;


        /**
         * 库存sku no
         */
        private String warehouseSkuNo;

        /**
         * 库存产品名
         */
        private String warehouseProductName;
    }


}
