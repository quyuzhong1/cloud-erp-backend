package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

/**
 * 收货明细DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
@NoArgsConstructor
public class WarehouseReceiveDetailDTO {

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 采购订单明细表id
         */
        private String purchaseOrderDetailId;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 收货单详情表id
         */
        private String id;

        /**
         * 收货单主表id
         */
        private String main_id;

        /**
         * 采购单详情表id
         */
        private String PurchaseOrderDetailId;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 查询详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 收货单明细表id
         */
        private String id;

        /**
         * 收货单主表id
         */
        private String mainId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划交货时间
         */
        private LocalDate planDeliveryDate;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 未签收数量
         */
        private Integer unReceiveQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 采购单详情表id
         */
        private String PurchaseOrderDetailId;
    }

}
