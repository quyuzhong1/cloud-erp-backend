package com.erp.server.dmp.entity.mabang;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class SkuInfoEntity {
    private String salesSku;
    private String stockSku;
    private String nameCN;
    private String nameEN;
    private BigDecimal defaultCost;
    private String forecastDaySale;
    private Integer hasBattery;
    private Integer isNewType;
    private Integer isTort;
    private Integer livenessType;
    private Integer status;
    private String timeCreated;
    private String timeModify;
    private String originalSku;
    private String brandName;
    private String parentCategoryName;
    private String categoryName;
    private BigDecimal salePrice;
    private BigDecimal declareValue;
    private String stockPicture;
    private String salePicture;
    private String length;
    private String width;
    private String height;
    private String weight;
    private String declareName;
    private String declareEname;
    private String remark;
    private String saleRemark;
    private String purchaseRemark;
    @SerializedName("package")
    private String packageX;
    private String declareCode;
    private Integer isGift;
    private Integer magnetic;
    private Integer powder;
    private String purchasePrice;
    private String developerId;
    private String developerName;
    private Integer buyerId;
    private String buyerName;
    private Integer artDesignerId;
    private String artDesignerName;
    private String provider;
    private String productLinkAddress;
    private String financial;
    private String commodityUse;
    private String commodityMaterial;
    private List<?> stockDetailImg;
    private List<?> sales;
    private List<?> virtualSku;
    private List<?> warehouse;
    private List<?> label;
    private List<?> attributes;
}
