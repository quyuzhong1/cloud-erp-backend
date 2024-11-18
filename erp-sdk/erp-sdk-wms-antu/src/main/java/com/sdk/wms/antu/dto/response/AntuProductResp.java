package com.sdk.wms.antu.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.config.FastJson2LocalDateTimeDeserializer;
import com.common.business.dto.CleanBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AntuProductResp extends CleanBaseDTO implements Serializable {
    private static final long serialVersionUID = 2405172041950251807L;
    //商品sku
    @JSONField(name = "product_sku")
    private String productSku;

    //客户参考代码
    @JSONField(name = "reference_no")
    private String referenceNo;

    //产品状态 X:废弃 D:草稿 S:可用 P:审核中 R:审核不通过
    @JSONField(name = "product_status")
    private String productStatus;

    //产品标题
    @JSONField(name = "product_title")
    private String productTitle;

    //产品标题英文
    @JSONField(name = "product_title_en")
    private String productTitleEn;

    //重量
    @JSONField(name = "product_weight")
    private Float productWeight;

    //长
    @JSONField(name = "product_length")
    private Float productLength;

    //宽
    @JSONField(name = "product_width")
    private Float productWidth;

    //高
    @JSONField(name = "product_height")
    private Float productHeight;

    //是否含电池，0不含，1含电池
    @JSONField(name = "contain_battery")
    private Integer containBattery;

    //申报价值，币种为USD
    @JSONField(name = "product_declared_value")
    private Float productDeclaredValue;

    //申报名称
    @JSONField(name = "product_declared_name")
    private String productDeclaredName;

    //品类语言，zh中文，en英文，默认为en
    @JSONField(name = "cat_lang")
    private String catLang;

    //产品品牌
    @JSONField(name = "product_brand")
    private String productBrand;

    //产品型号
    @JSONField(name = "product_model")
    private String productModel;

    //产品原产地
    @JSONField(name = "product_origin")
    private String productOrigin;

    //产品材质
    @JSONField(name = "product_material")
    private String productMaterial;

    //产品信息链接
    @JSONField(name = "product_desc_url")
    private String productDescUrl;

    //一级品类,参考getCategory
    @JSONField(name = "cat_id_level0")
    private Integer catIdLevel0;

    //二级品类,参考getCategory
    @JSONField(name = "cat_id_level1")
    private Integer catIdLevel1;

    //三级品类,参考getCategory
    @JSONField(name = "cat_id_level2")
    private Integer catIdLevel2;

    //添加时间
    @JSONField(name = "product_add_time",deserializeUsing = FastJson2LocalDateTimeDeserializer.class)
    private LocalDateTime productAddTime;

    //修改时间
    @JSONField(name = "product_modify_time",deserializeUsing = FastJson2LocalDateTimeDeserializer.class)
    private LocalDateTime productModifyTime;

    //产品价格
    @JSONField(name = "product_cost")
    private Float productCost;

    //客户币种
    @JSONField(name = "currency")
    private String currency;

    //客户代码
    @JSONField(name = "companyCode")
    private String companyCode;

    //产品图片
    @JSONField(name = "product_img")
    private String productImg;

    //产品仓库属性
    @JSONField(name = "warehouse_attribute")
    private List<WarehouseAttribute> warehouseAttribute;

    @Data
    @ToString
    public static class WarehouseAttribute implements Serializable{

        //长
        @JSONField(name = "product_length")
        private String productLength;

        //宽度
        @JSONField(name = "product_width")
        private String productWidth;

        //高
        @JSONField(name = "product_height")
        private String productHeight;

        //重量
        @JSONField(name = "product_weight")
        private String productWeight;
    }

}
