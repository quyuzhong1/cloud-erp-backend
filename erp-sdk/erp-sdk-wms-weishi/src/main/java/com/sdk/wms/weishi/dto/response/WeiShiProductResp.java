package com.sdk.wms.weishi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiProductResp {

    @JsonProperty("total")
    private Integer total;
    @JsonProperty("list")
    private List<ListDTO> list;

    @NoArgsConstructor
    @Data
    public static class ListDTO {
        @JsonProperty("product_sku")
        private String productSku;
        @JsonProperty("goods_barcode")
        private Object goodsBarcode;
        @JsonProperty("reference_no")
        private String referenceNo;
        @JsonProperty("customer_code")
        private String customerCode;
        @JsonProperty("customer_id")
        private Integer customerId;
        @JsonProperty("customer_short_name")
        private String customerShortName;
        @JsonProperty("product_title")
        private String productTitle;
        @JsonProperty("product_title_en")
        private String productTitleEn;
        @JsonProperty("product_status")
        private Integer productStatus;
        @JsonProperty("del_flag")
        private Object delFlag;
        @JsonProperty("product_material")
        private String productMaterial;
        @JsonProperty("hs_code")
        private String hsCode;
        @JsonProperty("contain_battery")
        private Integer containBattery;
        @JsonProperty("product_desc")
        private Object productDesc;
        @JsonProperty("product_declared_value")
        private String productDeclaredValue;
        @JsonProperty("product_declared_name")
        private String productDeclaredName;
        @JsonProperty("product_declared_name_en")
        private String productDeclaredNameEn;
        @JsonProperty("currency_code")
        private String currencyCode;
        @JsonProperty("unit_code")
        private String unitCode;
        @JsonProperty("main_img_path")
        private Object mainImgPath;
        @JsonProperty("main_img_name")
        private Object mainImgName;
        @JsonProperty("price")
        private Object price;
        @JsonProperty("category_id")
        private Integer categoryId;
        @JsonProperty("category_name")
        private String categoryName;
        @JsonProperty("category_name_cn")
        private String categoryNameCn;
        @JsonProperty("create_by")
        private String createBy;
        @JsonProperty("create_time")
        private String createTime;
        @JsonProperty("update_by")
        private String updateBy;
        @JsonProperty("update_time")
        private String updateTime;
        @JsonProperty("revision")
        private Integer revision;
        @JsonProperty("wdh_audit")
        private Object wdhAudit;
        @JsonProperty("product_url")
        private String productUrl;
        @JsonProperty("whs_overseas_limit_grade")
        private Object whsOverseasLimitGrade;
        @JsonProperty("cus_product_code")
        private Object cusProductCode;
        @JsonProperty("is_insure")
        private Integer isInsure;
        @JsonProperty("insure_scale")
        private Object insureScale;
        @JsonProperty("is_liquid")
        private Integer isLiquid;
        @JsonProperty("is_magnetic")
        private Integer isMagnetic;
        @JsonProperty("is_contains_dust")
        private Integer isContainsDust;
        @JsonProperty("is_special")
        private Integer isSpecial;
        @JsonProperty("is_unique_code")
        private Integer isUniqueCode;
        @JsonProperty("is_multi_specs")
        private Integer isMultiSpecs;
        @JsonProperty("box_barcode_one")
        private Object boxBarcodeOne;
        @JsonProperty("box_barcode_two")
        private Object boxBarcodeTwo;
        @JsonProperty("box_specs_one")
        private Object boxSpecsOne;
        @JsonProperty("box_specs_two")
        private Object boxSpecsTwo;
        @JsonProperty("cus_length")
        private String cusLength;
        @JsonProperty("cus_width")
        private String cusWidth;
        @JsonProperty("cus_height")
        private String cusHeight;
        @JsonProperty("cus_weight")
        private String cusWeight;
        @JsonProperty("length")
        private String length;
        @JsonProperty("width")
        private String width;
        @JsonProperty("height")
        private String height;
        @JsonProperty("weight")
        private String weight;
        @JsonProperty("seasonal_goods")
        private Boolean seasonalGoods;
        @JsonProperty("seasonal_sale_period")
        private String seasonalSalePeriod;
    }
}
