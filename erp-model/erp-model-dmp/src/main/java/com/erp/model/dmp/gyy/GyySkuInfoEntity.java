package com.erp.model.dmp.gyy;

import com.erp.model.dmp.gyy.bean.CustomAttrBean;
import com.erp.model.dmp.gyy.bean.CombineItemsBean;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class GyySkuInfoEntity {
    /**
     * id : 399161543391
     * create_date : 2021-10-25 16:42:33
     * modify_date : 2022-11-21 18:19:25
     * code : 2742+2745+2747+2982
     * name : VESA支臂管夹套餐
     * note : 
     * weight : 0.0
     * combine : true
     * del : false
     * length : 0.0
     * width : 0.0
     * height : 0.0
     * volume : 0.0
     * simple_name : 
     * category_code : null
     * category_name : null
     * supplier_code : null
     * item_unit_code : null
     * item_unit_name : null
     * package_point : 0.0
     * sales_point : 0.0
     * sales_price : 0.0
     * purchase_price : 0.0
     * agent_price : 0.0
     * cost_price : 0.0
     * stock_status_code : null
     * pic_url : null
     * tax_no : null
     * tax_rate : 0.0
     * origin_area : null
     * supplier_outerid : null
     * shelf_life : 0
     * warning_days : 0
     * skus : []
     * combine_items : [{"id":399161532472,"create_date":"2021-10-25 16:42:33","modify_date":"2022-11-21 18:19:25","qty":1,"percent":0.2033,"item_code":"2742","item_name":"FALCAM Geartree 单孔拓展座 - FALCAM Geartree 单孔拓展座","simple_name":"2742  FALCAM Geartree 单孔拓展座","item_sku_code":null,"item_sku_name":null,"goods_id":381102910491,"sales_price":0},{"id":399161540588,"create_date":"2021-10-25 16:42:33","modify_date":"2022-11-21 18:19:25","qty":1,"percent":0,"item_code":"2982","item_name":"FALCAM GEARTREE VESA拓展板PRO","simple_name":"FALCAM GEARTREE VESA拓展板PRO","item_sku_code":null,"item_sku_name":null,"goods_id":468600010334,"sales_price":0},{"id":399161561781,"create_date":"2021-10-25 16:42:33","modify_date":"2022-11-21 18:19:25","qty":1,"percent":0.6271,"item_code":"2745","item_name":"FALCAM Geartree 多功能支臂L - FALCAM Geartree 多功能支臂L","simple_name":"2745  FALCAM Geartree 多功能支臂L","item_sku_code":null,"item_sku_name":null,"goods_id":381102899126,"sales_price":0},{"id":399161566661,"create_date":"2021-10-25 16:42:33","modify_date":"2022-11-21 18:19:25","qty":1,"percent":0.1696,"item_code":"2747","item_name":"FALCAM Geartree 横臂S - FALCAM Geartree 横臂S","simple_name":"2747  FALCAM Geartree 横臂S","item_sku_code":null,"item_sku_name":null,"goods_id":381102904028,"sales_price":0}]
     * custom_attr : {}
     * item_add_attribute : 0
     * item_brand_id : null
     * item_brand_code : null
     * item_brand_name : null
     * goods_id : 399161562707
     */
    @JsonProperty("id")
    private String id;
    @SerializedName("create_date")
    private String createDate;
    @SerializedName("modify_date")
    private String modifyDate;
    @SerializedName("code")
    private String code;
    @SerializedName("name")
    private String name;
    @SerializedName("note")
    private String note;
    @SerializedName("weight")
    private BigDecimal weight;
    @SerializedName("combine")
    private Boolean combine;
    @SerializedName("del")
    private Boolean del;
    @SerializedName("length")
    private BigDecimal length;
    @SerializedName("width")
    private BigDecimal width;
    @SerializedName("height")
    private BigDecimal height;
    @SerializedName("volume")
    private BigDecimal volume;
    @SerializedName("simple_name")
    private String simpleName;
    @SerializedName("category_code")
    private Object categoryCode;
    @SerializedName("category_name")
    private Object categoryName;
    @SerializedName("supplier_code")
    private Object supplierCode;
    @SerializedName("item_unit_code")
    private Object itemUnitCode;
    @SerializedName("item_unit_name")
    private Object itemUnitName;
    @SerializedName("package_point")
    private BigDecimal packagePoint;
    @SerializedName("sales_point")
    private BigDecimal salesPoint;
    @SerializedName("sales_price")
    private BigDecimal salesPrice;
    @SerializedName("purchase_price")
    private BigDecimal purchasePrice;
    @SerializedName("agent_price")
    private BigDecimal agentPrice;
    @SerializedName("cost_price")
    private BigDecimal costPrice;
    @SerializedName("stock_status_code")
    private Object stockStatusCode;
    @SerializedName("pic_url")
    private Object picUrl;
    @SerializedName("tax_no")
    private Object taxNo;
    @SerializedName("tax_rate")
    private BigDecimal taxRate;
    @SerializedName("origin_area")
    private Object originArea;
    @SerializedName("supplier_outerid")
    private Object supplierOuterid;
    @SerializedName("shelf_life")
    private Integer shelfLife;
    @SerializedName("warning_days")
    private Integer warningDays;
    @SerializedName("custom_attr")
    private CustomAttrBean customAttr;
    @SerializedName("item_add_attribute")
    private Integer itemAddAttribute;
    @SerializedName("item_brand_id")
    private String itemBrandId;
    @SerializedName("item_brand_code")
    private String itemBrandCode;
    @SerializedName("item_brand_name")
    private String itemBrandName;
    @SerializedName("goods_id")
    private Long goodsId;
    @SerializedName("skus")
    private List<?> skus;
    @SerializedName("combine_items")
    private List<CombineItemsBean> combineItems;
    /**
     * 清洗数据
     */
    private Boolean isClean;
    
}
