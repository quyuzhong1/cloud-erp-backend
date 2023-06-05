package com.erp.model.dmp.gyy.bean;

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

    /**
     * 组合商品ID
     */
    @SerializedName("id")
    private Long id;
    /**
     * 创建时间
     */
    @SerializedName("create_date")
    private String createDate;
    /**
     * 修改时间
     */
    @SerializedName("modify_date")
    private String modifyDate;
    /**
     * 数量
     */
    @SerializedName("qty")
    private Integer qty;
    /**
     * 权重比例
     */
    @SerializedName("percent")
    private Integer percent;
    /**
     * 明细商品的商品代码
     */
    @SerializedName("item_code")
    private String itemCode;
    /**
     * 明细商品的商品名称
     */
    @SerializedName("item_name")
    private String itemName;
    /**
     * 明细商品的商品简称
     */
    @SerializedName("simple_name")
    private String simpleName;
    /**
     * 明细商品的商品代码
     */
    @SerializedName("item_sku_code")
    private Object itemSkuCode;
    /**
     * 明细商品的商品名称
     */
    @SerializedName("item_sku_name")
    private Object itemSkuName;
    /**
     *  商品ID
     */
    @SerializedName("goods_id")
    private long goodsId;
    /**
     * 明细商品的标准售价
     */
    @SerializedName("sales_price")
    private double salesPrice;


}
