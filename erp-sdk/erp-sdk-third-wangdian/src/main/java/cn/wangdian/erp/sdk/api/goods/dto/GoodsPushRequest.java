package cn.wangdian.erp.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class GoodsPushRequest
{
    public static class GoodsDto
    {
        private String goodsName;
        private String goodsNo;
        private String origin;
        private String remark;

        @SerializedName("brand_name")
        private String brandName;
        @SerializedName("aux_unit_name")
        private String auxUnitName;
        private String prop6;
        private String prop5;
        private String prop4;
        @SerializedName("flag_name")
        private String flagName;
        private String prop3;
        private String prop2;
        @SerializedName("unit_name")
        private String unitName;
        private String prop1;
        private String pinyin;
        private String alias;
        private String shortName;
        private Integer specCount;
        private Byte goodsType;
        @SerializedName("class_name")
        private String className;
        private Integer classId;
        private Integer deleted;
        private boolean autoCreateBc;

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public String getGoodsNo() {
            return goodsNo;
        }

        public void setGoodsNo(String goodsNo) {
            this.goodsNo = goodsNo;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }

        public String getAuxUnitName() {
            return auxUnitName;
        }

        public void setAuxUnitName(String auxUnitName) {
            this.auxUnitName = auxUnitName;
        }

        public String getProp6() {
            return prop6;
        }

        public void setProp6(String prop6) {
            this.prop6 = prop6;
        }

        public String getProp5() {
            return prop5;
        }

        public void setProp5(String prop5) {
            this.prop5 = prop5;
        }

        public String getProp4() {
            return prop4;
        }

        public void setProp4(String prop4) {
            this.prop4 = prop4;
        }

        public String getFlagName() {
            return flagName;
        }

        public void setFlagName(String flagName) {
            this.flagName = flagName;
        }

        public String getProp3() {
            return prop3;
        }

        public void setProp3(String prop3) {
            this.prop3 = prop3;
        }

        public String getProp2() {
            return prop2;
        }

        public void setProp2(String prop2) {
            this.prop2 = prop2;
        }

        public String getUnitName() {
            return unitName;
        }

        public void setUnitName(String unitName) {
            this.unitName = unitName;
        }

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public String getPinyin() {
            return pinyin;
        }

        public void setPinyin(String pinyin) {
            this.pinyin = pinyin;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public Integer getSpecCount() {
            return specCount;
        }

        public void setSpecCount(Integer specCount) {
            this.specCount = specCount;
        }

        public Byte getGoodsType() {
            return goodsType;
        }

        public void setGoodsType(Byte goodsType) {
            this.goodsType = goodsType;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Integer getClassId() {
            return classId;
        }

        public void setClassId(Integer classId) {
            this.classId = classId;
        }

        public Integer getDeleted() {
            return deleted;
        }

        public void setDeleted(Integer deleted) {
            this.deleted = deleted;
        }

        public boolean isAutoCreateBc() {
            return autoCreateBc;
        }

        public void setAutoCreateBc(boolean autoCreateBc) {
            this.autoCreateBc = autoCreateBc;
        }
    }

    public static class GoodsSpecDto
    {
        private String specNo;
        private String specCode;
        private String barcode;
        private String specName;
        private BigDecimal lowestPrice;
        private BigDecimal retailPrice;
        private BigDecimal wholesalePrice;
        private BigDecimal memberPrice;
        private BigDecimal marketPrice;
        private BigDecimal validityDays;
        private BigDecimal salesDays;
        private BigDecimal receiveDays;
        private BigDecimal weight;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal snType;
        private BigDecimal saleScore;
        private BigDecimal packScore;
        private BigDecimal pickScore;
        private BigDecimal sortScore;
        private BigDecimal scanScore;
        private BigDecimal supplyScore;
        private BigDecimal shelveScore;
        private BigDecimal stockinScore;
        private BigDecimal inspectScore;
        private BigDecimal packingScore;
        private BigDecimal operateScore;
        private BigDecimal weighScore;
        private BigDecimal consignScore;
        @SerializedName("is_lower_cost")
        private Boolean lowerCost;
        @SerializedName("is_not_use_air")
        private Boolean notUseAir;
   //     private BigDecimal taxRate;
        private Byte wmsProcessMask;
        private Byte largeType;
        private String remark;
        private String specModified;
        private String specCreated;
        private String prop1;
        private String prop2;
        private String prop3;
        private String prop4;
        private String prop5;
        private String prop6;
        private String imgUrl;
        @SerializedName("unit_name")
        private String specUnitName;
        @SerializedName("aux_unit_name")
        private String specAuxUnitName;
        private String goodsLabel;
        private String taxCode;
        private Integer deleted;
        private BigDecimal customPrice1;
        private BigDecimal customPrice2;


        public String getSpecNo() {
            return specNo;
        }

        public void setSpecNo(String specNo) {
            this.specNo = specNo;
        }

        public String getSpecCode() {
            return specCode;
        }

        public void setSpecCode(String specCode) {
            this.specCode = specCode;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getSpecName() {
            return specName;
        }

        public void setSpecName(String specName) {
            this.specName = specName;
        }

        public BigDecimal getLowestPrice() {
            return lowestPrice;
        }

        public void setLowestPrice(BigDecimal lowestPrice) {
            this.lowestPrice = lowestPrice;
        }

        public BigDecimal getRetailPrice() {
            return retailPrice;
        }

        public void setRetailPrice(BigDecimal retailPrice) {
            this.retailPrice = retailPrice;
        }

        public BigDecimal getWholesalePrice() {
            return wholesalePrice;
        }

        public void setWholesalePrice(BigDecimal wholesalePrice) {
            this.wholesalePrice = wholesalePrice;
        }

        public BigDecimal getMemberPrice() {
            return memberPrice;
        }

        public void setMemberPrice(BigDecimal memberPrice) {
            this.memberPrice = memberPrice;
        }

        public BigDecimal getMarketPrice() {
            return marketPrice;
        }

        public void setMarketPrice(BigDecimal marketPrice) {
            this.marketPrice = marketPrice;
        }

        public BigDecimal getValidityDays() {
            return validityDays;
        }

        public void setValidityDays(BigDecimal validityDays) {
            this.validityDays = validityDays;
        }

        public BigDecimal getSalesDays() {
            return salesDays;
        }

        public void setSalesDays(BigDecimal salesDays) {
            this.salesDays = salesDays;
        }

        public BigDecimal getReceiveDays() {
            return receiveDays;
        }

        public void setReceiveDays(BigDecimal receiveDays) {
            this.receiveDays = receiveDays;
        }

        public BigDecimal getWeight() {
            return weight;
        }

        public void setWeight(BigDecimal weight) {
            this.weight = weight;
        }

        public BigDecimal getLength() {
            return length;
        }

        public void setLength(BigDecimal length) {
            this.length = length;
        }

        public BigDecimal getWidth() {
            return width;
        }

        public void setWidth(BigDecimal width) {
            this.width = width;
        }

        public BigDecimal getHeight() {
            return height;
        }

        public void setHeight(BigDecimal height) {
            this.height = height;
        }

        public BigDecimal getSnType() {
            return snType;
        }

        public void setSnType(BigDecimal snType) {
            this.snType = snType;
        }

        public BigDecimal getSaleScore() {
            return saleScore;
        }

        public void setSaleScore(BigDecimal saleScore) {
            this.saleScore = saleScore;
        }

        public BigDecimal getPackScore() {
            return packScore;
        }

        public void setPackScore(BigDecimal packScore) {
            this.packScore = packScore;
        }

        public BigDecimal getPickScore() {
            return pickScore;
        }

        public void setPickScore(BigDecimal pickScore) {
            this.pickScore = pickScore;
        }

        public BigDecimal getSortScore() {
            return sortScore;
        }

        public void setSortScore(BigDecimal sortScore) {
            this.sortScore = sortScore;
        }

        public BigDecimal getScanScore() {
            return scanScore;
        }

        public void setScanScore(BigDecimal scanScore) {
            this.scanScore = scanScore;
        }

        public BigDecimal getSupplyScore() {
            return supplyScore;
        }

        public void setSupplyScore(BigDecimal supplyScore) {
            this.supplyScore = supplyScore;
        }

        public BigDecimal getShelveScore() {
            return shelveScore;
        }

        public void setShelveScore(BigDecimal shelveScore) {
            this.shelveScore = shelveScore;
        }

        public BigDecimal getStockinScore() {
            return stockinScore;
        }

        public void setStockinScore(BigDecimal stockinScore) {
            this.stockinScore = stockinScore;
        }

        public BigDecimal getInspectScore() {
            return inspectScore;
        }

        public void setInspectScore(BigDecimal inspectScore) {
            this.inspectScore = inspectScore;
        }

        public BigDecimal getPackingScore() {
            return packingScore;
        }

        public void setPackingScore(BigDecimal packingScore) {
            this.packingScore = packingScore;
        }

        public BigDecimal getOperateScore() {
            return operateScore;
        }

        public void setOperateScore(BigDecimal operateScore) {
            this.operateScore = operateScore;
        }

        public BigDecimal getWeighScore() {
            return weighScore;
        }

        public void setWeighScore(BigDecimal weighScore) {
            this.weighScore = weighScore;
        }

        public BigDecimal getConsignScore() {
            return consignScore;
        }

        public void setConsignScore(BigDecimal consignScore) {
            this.consignScore = consignScore;
        }

        public Boolean getLowerCost() {
            return lowerCost;
        }

        public void setLowerCost(Boolean lowerCost) {
            this.lowerCost = lowerCost;
        }

        public Boolean getNotUseAir() {
            return notUseAir;
        }

        public void setNotUseAir(Boolean notUseAir) {
            this.notUseAir = notUseAir;
        }

        public Byte getWmsProcessMask() {
            return wmsProcessMask;
        }

        public void setWmsProcessMask(Byte wmsProcessMask) {
            this.wmsProcessMask = wmsProcessMask;
        }

        public Byte getLargeType() {
            return largeType;
        }

        public void setLargeType(Byte largeType) {
            this.largeType = largeType;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getSpecModified() {
            return specModified;
        }

        public void setSpecModified(String specModified) {
            this.specModified = specModified;
        }

        public String getSpecCreated() {
            return specCreated;
        }

        public void setSpecCreated(String specCreated) {
            this.specCreated = specCreated;
        }

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public String getProp2() {
            return prop2;
        }

        public void setProp2(String prop2) {
            this.prop2 = prop2;
        }

        public String getProp3() {
            return prop3;
        }

        public void setProp3(String prop3) {
            this.prop3 = prop3;
        }

        public String getProp4() {
            return prop4;
        }

        public void setProp4(String prop4) {
            this.prop4 = prop4;
        }

        public String getProp5() {
            return prop5;
        }

        public void setProp5(String prop5) {
            this.prop5 = prop5;
        }

        public String getProp6() {
            return prop6;
        }

        public void setProp6(String prop6) {
            this.prop6 = prop6;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getSpecUnitName() {
            return specUnitName;
        }

        public void setSpecUnitName(String specUnitName) {
            this.specUnitName = specUnitName;
        }

        public String getSpecAuxUnitName() {
            return specAuxUnitName;
        }

        public void setSpecAuxUnitName(String specAuxUnitName) {
            this.specAuxUnitName = specAuxUnitName;
        }

        public String getGoodsLabel() {
            return goodsLabel;
        }

        public void setGoodsLabel(String goodsLabel) {
            this.goodsLabel = goodsLabel;
        }

        public String getTaxCode() {
            return taxCode;
        }

        public void setTaxCode(String taxCode) {
            this.taxCode = taxCode;
        }

        public Integer getDeleted() {
            return deleted;
        }

        public void setDeleted(Integer deleted) {
            this.deleted = deleted;
        }

        public BigDecimal getCustomPrice1() {
            return customPrice1;
        }

        public void setCustomPrice1(BigDecimal customPrice1) {
            this.customPrice1 = customPrice1;
        }

        public BigDecimal getCustomPrice2() {
            return customPrice2;
        }

        public void setCustomPrice2(BigDecimal customPrice2) {
            this.customPrice2 = customPrice2;
        }
    }
}
