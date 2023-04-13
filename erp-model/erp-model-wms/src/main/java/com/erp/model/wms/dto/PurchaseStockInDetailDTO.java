package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/13 10:25
 */
@Data
@NoArgsConstructor
public class PurchaseStockInDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 本次入库数量
         */
        private String stockInQty;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 采购数量
         */
        private String purchaseQty;

        /**
         * 收货数量
         */
        private String receiveQty;

        /**
         * 超收数量
         */
        private String exceedQty;

        /**
         * 库位id
         */
        private String warehouseLocationId;

        /**
         * 备注
         */
        private String remark;

        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends  AddDTO{
        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends  UpdateDTO{
        /**
         * 未入库数量
         */
       private Integer unStockInQty;
    }
}
