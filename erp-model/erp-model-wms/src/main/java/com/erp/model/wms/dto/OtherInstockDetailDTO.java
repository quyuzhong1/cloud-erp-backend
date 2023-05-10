package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 12:15
 */
@Data
@NoArgsConstructor
public class OtherInstockDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuId
         */
        private String  skuId;
        /**
         * sku编码
         */
        private String  skuNo;
        /**
         * 应收数量
         */
        private String  planQty;
        /**
         * 实收数量
         */
        private String  actualQty;
        /**
         * 收货仓库id
         */
        private String  warehouseId;
        /**
         * 库位id
         */
        private String  warehouseLocation;
        /**
         * 备注
         */
        private String  remark;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

    }
}
