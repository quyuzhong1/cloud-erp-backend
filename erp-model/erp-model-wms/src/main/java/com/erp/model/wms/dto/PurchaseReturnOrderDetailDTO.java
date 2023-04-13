package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Date;

/**
 * 收货明细DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
@NoArgsConstructor
public class PurchaseReturnOrderDetailDTO {

    /**
     * 添加
     */
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
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 退货单价
         */
        private String returnPrice;

        /**
         * 总价
         */
        private String totalPrice;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 退货单详情表id
         */
        private String id;

        /**
         * 采购单详情表id
         */
        private String purchaseDetailId;

        /**
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

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
         * 退货单明细表id
         */
        private String id;

        /**
         * 退货单主表id
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
         * 已入库数量
         */
        private String stockInQty;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 退货单价
         */
        private String returnPrice;

        /**
         * 总价
         */
        private String totalPrice;

        /**
         * 备注
         */
        private String remark;
    }

}
