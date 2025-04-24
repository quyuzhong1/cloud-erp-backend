package com.erp.model.wms.dto.renovation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SecondarySortingDTO {
    private SecondarySortingDTO() {
        throw new IllegalStateException("Utility SecondarySortingDTO class");
    }

    @Getter
    @Setter
    public static class ScanCodeView {

        /**
         * 波次id
         */
        private String waveId;
        /**
         * 波次号
         */
        private String code;
        /**
         * 篮子数
         */
        private Integer basketQty;
        /**
         * 篮子明细
         */
        private List<BasketDTO> basketDetails;
    }

    @Getter
    @Setter
    public static class BasketDTO {

        /**
         * 篮号
         */
        private String basketNo;
        /**
         * 拣货总数
         */
        private Integer pickingQty;
        /**
         * 已分货数量
         */
        private Integer allocatedQty;
        /**
         * 是否拦截
         */
        private Boolean isIntercept;
        /**
         * 是否缺货
         */
        private Boolean isOutStock;
    }

    @Getter
    @Setter
    public static class ScanSkuView {

        /**
         * 篮号
         */
        private String basketNo;
        /**
         * sku
         */
        private String skuNo;
        /**
         * sku id
         */
        private String skuId;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 已分货数量
         */
        private Integer allocatedQty;
    }

    @Getter
    @Setter
    public static class BasketDetail {

        /**
         * sku id
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 篮号
         */
        private String basketNo;
        /**
         * ean
         */
        private String ean;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 已分货数量
         */
        private Integer allocatedQty;
    }
}
