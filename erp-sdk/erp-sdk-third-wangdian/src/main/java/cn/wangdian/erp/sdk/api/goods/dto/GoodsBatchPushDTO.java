package cn.wangdian.erp.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class GoodsBatchPushDTO {
    @SerializedName("goods_no")
    private String goodsNo;
    @SerializedName("goods_name")
    private String goodsName;
    @SerializedName("class_name")
    private String className;
    @SerializedName("brand_name")
    private String brandName;
    @SerializedName("flag_name")
    private String flagName;
    @SerializedName("short_name")
    private String shortName;
    @SerializedName("auto_create_bc")
    private boolean autoCreateBc;
    @SerializedName("goods_type")
    private Integer goodsType;
    private String alias;
    private String pinyin;
    private String origin;
    private String remark;
    private String prop1;
    private String prop2;
    private String prop3;
    private String prop4;
    private String prop5;
    private String prop6;
    @SerializedName("spec_list")
    private List<SpecList> specList;


    @Getter
    @Setter
    public static class SpecList{
        @SerializedName("spec_no")
        private String specNo;
        @SerializedName("spec_code")
        private String specCode;
        private String barcode;
        @SerializedName("spec_name")
        private String specName;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal weight;
        @SerializedName("img_url")
        private String imgUrl;
        @SerializedName("unit_name")
        private String unitName;
        @SerializedName("pack_score")
        private int packScore;
        @SerializedName("lowest_price")
        private int lowestPrice;
        private String remark;
        @SerializedName("sn_type")
        private int snType;
        @SerializedName("retail_price")
        private int retailPrice;
        @SerializedName("wholesale_price")
        private int wholesalePrice;
        @SerializedName("market_price")
        private int marketPrice;
        @SerializedName("is_single_batch")
        private int isSingleBatch;
        @SerializedName("custom_price1")
        private int customPrice1;
        @SerializedName("custom_price2")
        private int customPrice2;
    }

}
