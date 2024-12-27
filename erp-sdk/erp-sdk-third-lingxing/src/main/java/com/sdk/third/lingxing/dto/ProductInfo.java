package com.sdk.third.lingxing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo implements Serializable {

    @NotNull
    private String sku;

    @NotNull
    private String productName;

    @NotNull
    private String skuIdentifier;

    private List<PictureInfo> pictureList;

    private String unit;

    private Integer categoryId;

    private String category;

    private String model;

    private Integer brandId;

    private String brand;

    private Integer status;

    private String description;

    private List<GroupInfo> groupList;

    private Integer cgOptUid;

    private String cgOptUsername;

    private Integer productDeveloperUid;

    private String productDeveloper;

    private Integer productCreatorUid;

    private List<Integer> productDutyUids;

    private Integer isAppendProductDuty;

    private String purchaseRemark;

    private String cgPrice;

    private Integer isRelated;

    private Integer cgDelivery;

    private String cgProductMaterial;

    private String cgProductLength;

    private String cgProductWidth;

    private String cgProductHeight;

    private String cgProductNetWeight;

    private String cgProductGrossWeight;

    private String cgPackageLength;

    private String cgPackageWidth;

    private String cgPackageHeight;

    private String cgBoxLength;

    private String cgBoxWidth;

    private String cgBoxHeight;

    private String cgBoxWeight;

    private Integer cgBoxPcs;

    private String bgCustomsExportName;

    private String bgExportHsCode;

    private String bgCustomsImportName;

    private String currency;

    private String bgCustomsImportPrice;

    private QCStandard qcStandard;

    private List<ProductLogisticsInfo> productLogisticsList;

    private List<SupplierQuote> supplierQuote;

    private List<Integer> specialAttr;

    private Declaration declaration;

    private Clearance clearance;

    public ProductInfo(String sku, String productName, String skuIdentifier) {
        this.sku = sku;
        this.productName = productName;
        this.skuIdentifier = skuIdentifier;
    }

    @Data
    @NoArgsConstructor
    public static class PictureInfo {
        private String picUrl;
        private Integer isPrimary;
    }


    @Data
    @NoArgsConstructor
    public static class GroupInfo {
        private String sku;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    public static class QCStandard {
        private CustomQCTemplate customQcTemplate;


        @Data
        @NoArgsConstructor
        public static class CustomQCTemplate {
            private List<QCImage> qcImage;

            @Data
            @NoArgsConstructor
            public static class QCImage {
                private String fileId;
                private String customerUrl;

            }
        }

    }

    @Data
    @NoArgsConstructor
    public static class ProductLogisticsInfo {
        private String usCgTransportCosts;
        private String usCurrency;
        private String usClearancePrice;
        private String usClearancePriceCurrency;
        private String usBgImportHsCode;
        private String usBgTaxRate;
    }


    @Data
    @NoArgsConstructor
    public static class SupplierQuote {
        private Integer erpSupplierId;
        private Integer supplierId;
        private List<String> supplierProductUrl;
        private String quoteRemark;
        private Integer isPrimary;
        private List<Quote> quotes;


        @Data
        @NoArgsConstructor
        public static class Quote {
            private String currency;
            private Integer isTax;
            private String taxRate;
            private List<StepPrice> stepPrices;

            @Data
            @NoArgsConstructor
            public static class StepPrice {
                private Integer moq;
                private String priceWithTax;
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class Declaration {
        private String customsDeclarationUnit;
        private String customsDeclarationSpec;
        private String customsDeclarationOriginProduce;
        private String customsDeclarationInlandsSource;
        private String customsDeclarationExempt;
    }

    @Data
    @NoArgsConstructor
    public static class Clearance {
        private String customsClearanceMaterial;
        private String customsClearanceUsage;
        private String customsClearanceInternalCode;
        private Integer customsClearancePreferential;
        private Integer customsClearanceBrandType;
        private String customsClearanceProductPattern;
        private String allocationRemark;
        private Integer weavingMode;
        private String customsClearancePicUrl;

    }


}
