package com.erp.model.scm.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购业务汇总表
 * @Author Luo_WG
 * @Date 2023/6/12 18:06
 **/
public class PurchaseBusinessGatherTableDTO extends PermissionsDTO implements Serializable {

    /**
     * 分页信息
     */
    @Data
    public static class PagingViewDTO {
        /**
         * id
         */
        private String id;
        /**
         * 采购组织
         */
        private String purchaseOrgName;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 产品图片
         */
        private String imagesUrl;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * spu型号
         */
        private String spuNo;
        /**
         * 平均价格
         */
        private BigDecimal avgPrice;
        /**
         * 订单数量
         */
        private Integer orderQty;
        /**
         * 含税单价
         */
        private BigDecimal taxPrice;
        /**
         * 订单金额
         */
        private BigDecimal orderAmount;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 收货赠品数量
         */
        private Integer receiveGiftQty;
        /**
         * 收货金额
         */
        private BigDecimal receiveAmount;
        /**
         * 入库数量
         */
        private Integer stockInQty;
        /**
         * 入库赠品数量
         */
        private Integer stockInGiftQty;
        /**
         * 入库金额
         */
        private BigDecimal stockInAmount;
        /**
         * 退货扣款数量
         */
        private Integer refundQty;
        /**
         * 退货补货数量
         */
        private Integer replenishQty;
        /**
         * 退货金额
         */
        private BigDecimal returnAmount;

        public PagingViewDTO() {
            this.avgPrice = BigDecimal.ZERO;
            this.taxPrice = BigDecimal.ZERO;
            this.orderAmount = BigDecimal.ZERO;
            this.receiveQty = 0;
            this.orderQty = 0;
            this.receiveGiftQty = 0;
            this.receiveAmount = BigDecimal.ZERO;
            this.stockInQty = 0;
            this.stockInGiftQty = 0;
            this.stockInAmount = BigDecimal.ZERO;
            this.refundQty = 0;
            this.replenishQty = 0;
            this.returnAmount = BigDecimal.ZERO;
        }
    }



    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * ids
         */
        private List<String> ids;
        /**
         * 单据日期范围
         */
        private List<LocalDate> billDateList;
        /**
         * sku集合
         */
        private List<String> skuNoList;
        /**
         * spu集合
         */
        private List<String> spuIdList;
        /**
         * 仓库id
         */
        private List<String> warehouseIdList;
        /**
         * 采购组织id
         */
        private List<String> purchaseOrgIdList;
        /**
         * 供应商id
         */
        private List<String> supplierIdList;
    }
}
