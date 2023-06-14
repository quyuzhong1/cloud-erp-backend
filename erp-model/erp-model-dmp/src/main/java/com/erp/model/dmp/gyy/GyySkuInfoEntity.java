package com.erp.model.dmp.gyy;

import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.gyy.bean.CustomAttrBean;
import com.erp.model.dmp.gyy.bean.CombineItemsBean;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@ToString
public class GyySkuInfoEntity extends CleanBaseDTO {
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
    /**
     * 商品id
     */
    @SerializedName("id")
    private String id;
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
     * 商品编码
     */
    @SerializedName("code")
    private String code;
    /**
     * 商品名称
     */
    @SerializedName("name")
    private String name;
    /**
     * 商品备注
     */
    @SerializedName("note")
    private String note;
    /**
     *  商品重量
     */
    @SerializedName("weight")
    private BigDecimal weight;
    /**
     * 是否组合商品
     */
    @SerializedName("combine")
    private Boolean combine;
    /**
     * 是否已停用
     */
    @SerializedName("del")
    private Boolean del;
    /**
     * 商品长度
     */
    @SerializedName("length")
    private BigDecimal length;
    /**
     * 商品宽度
     */
    @SerializedName("width")
    private BigDecimal width;
    /**
     * 商品高度
     */
    @SerializedName("height")
    private BigDecimal height;
    /**
     * 商品体积
     */
    @SerializedName("volume")
    private BigDecimal volume;
    /**
     * 商品简称
     */
    @SerializedName("simple_name")
    private String simpleName;
    /**
     * 分类代码
     */
    @SerializedName("category_code")
    private Object categoryCode;
    /**
     * 分类名称
     */
    @SerializedName("category_name")
    private Object categoryName;
    /**
     * 供应商代码
     */
    @SerializedName("supplier_code")
    private Object supplierCode;
    /**
     * 商品单位代码
     */
    @SerializedName("item_unit_code")
    private Object itemUnitCode;
    /**
     * 商品单位名称
     */
    @SerializedName("item_unit_name")
    private Object itemUnitName;
    /**
     * 打包积分
     */
    @SerializedName("package_point")
    private BigDecimal packagePoint;
    /**
     * 销售积分
     */
    @SerializedName("sales_point")
    private BigDecimal salesPoint;
    /**
     * 标准售价
     */
    @SerializedName("sales_price")
    private BigDecimal salesPrice;
    /**
     * 标准进价
     */
    @SerializedName("purchase_price")
    private BigDecimal purchasePrice;
    /**
     * 代理售价
     */
    @SerializedName("agent_price")
    private BigDecimal agentPrice;
    /**
     * 成本价
     */
    @SerializedName("cost_price")
    private BigDecimal costPrice;
    /**
     * 库存状态代码
     */
    @SerializedName("stock_status_code")
    private Object stockStatusCode;
    /**
     * 图片地址
     */
    @SerializedName("pic_url")
    private Object picUrl;
    /**
     * 税号
     */
    @SerializedName("tax_no")
    private Object taxNo;
    /**
     * 税率
     */
    @SerializedName("tax_rate")
    private BigDecimal taxRate;
    /**
     * 原产地
     */
    @SerializedName("origin_area")
    private Object originArea;
    /**
     * 供应商货号
     */
    @SerializedName("supplier_outerid")
    private Object supplierOuterid;
    /**
     * 保质期
     */
    @SerializedName("shelf_life")
    private Integer shelfLife;
    /**
     * 预警天数
     */
    @SerializedName("warning_days")
    private Integer warningDays;
    /**
     * 自定义属性
     */
    @SerializedName("custom_attr")
    private Map<Object,Object> customAttr;
    /**
     * 商品附加属性 0:普通商品 1:唯一码商品 2:批次商品
     */
    @SerializedName("item_add_attribute")
    private Integer itemAddAttribute;
    /**
     * 商品品牌id
     */
    @SerializedName("item_brand_id")
    private String itemBrandId;
    /**
     * 商品品牌编码
     */
    @SerializedName("item_brand_code")
    private String itemBrandCode;
    /**
     * 商品品牌名称
     */
    @SerializedName("item_brand_name")
    private String itemBrandName;
    /**
     * 商品id
     */
    @SerializedName("goods_id")
    private Long goodsId;
    /**
     * 商品规格
     */
    @SerializedName("skus")
    private List<Object> skus;
    /**
     * 组合明细
     */
    @SerializedName("combine_items")
    private List<CombineItemsBean> combineItems;
}
