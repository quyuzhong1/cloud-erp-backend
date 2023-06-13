package com.erp.model.scm.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 采购组织
         */
        private String purchaseOrgName;
        /**
         * 供应商名称
         */
        private String supplierName;
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
        private String avgPrice;
        /**
         * 订单数量
         */
        private String orderQty;
        /**
         * 订单金额
         */
        private String orderAmount;
        /**
         * 收货数量
         */
        private String receiveQty;
        /**
         * 赠品数量
         */
        private String giftQty;
        /**
         * 收货金额
         */
        private String receiveAmount;
        /**
         * 入库数量
         */
        private String stockInQty;
        /**
         * 超收数量
         */
        private String exceedQty;
        /**
         * 入库金额
         */
        private String stockInAmount;
        /**
         * 退货扣款数量
         */
        private String refundQty;
        /**
         * 退货补货数量
         */
        private String replenishQty;
        /**
         * 退货金额
         */
        private String returnAmount;
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
        private List<String> billDateList;
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
