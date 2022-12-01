package com.erp.server.dmp.entity.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class CombineItemsBean {
    /**
     * id : 399161532472
     * create_date : 2021-10-25 16:42:33
     * modify_date : 2022-11-21 18:19:25
     * qty : 1.0
     * percent : 0.2033
     * item_code : 2742
     * item_name : FALCAM Geartree 单孔拓展座 - FALCAM Geartree 单孔拓展座
     * simple_name : 2742  FALCAM Geartree 单孔拓展座
     * item_sku_code : null
     * item_sku_name : null
     * goods_id : 381102910491
     * sales_price : 0.0
     */

    @SerializedName("id")
    private Long id;
    @SerializedName("create_date")
    private String createDate;
    @SerializedName("modify_date")
    private String modifyDate;
    @SerializedName("qty")
    private Integer qty;
    @SerializedName("percent")
    private Integer percent;
    @SerializedName("item_code")
    private String itemCode;
    @SerializedName("item_name")
    private String itemName;
    @SerializedName("simple_name")
    private String simpleName;
    @SerializedName("item_sku_code")
    private Object itemSkuCode;
    @SerializedName("item_sku_name")
    private Object itemSkuName;
    @SerializedName("goods_id")
    private long goodsId;
    @SerializedName("sales_price")
    private double salesPrice;


}
