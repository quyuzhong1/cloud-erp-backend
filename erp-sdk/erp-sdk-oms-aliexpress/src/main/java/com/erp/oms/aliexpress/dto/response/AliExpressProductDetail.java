package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname AliExpressProduct
 * @Description  商品明细
 * @Date 2023-11-30 9:00
 * @Created by yl
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AliExpressProductDetail implements Serializable {


    @JSONField(name = "sku_Id")
    private String skuId;

    /**
     * ku商家编码。 格式:半角英数字,长度20,不包含空格大于号和小于号。如果用户只填写零售价（productprice）和商品编码，
     * 需要完整生成一条SKU记录提交，否则商品编码无法保存。系统会认为只提交了零售价，而没有SKU，导致商品编辑未保存。
     */
    @JSONField(name = "sku_code")
    private String skuCode;

    /**
     * Sku价格。取值范围:0.01-100000;单位:美元。 如:200.07，表示:200美元7分。需要在正确的价格区间内。
     */
    @JSONField(name = "sku_price")
    private String skuPrice;

    /**
     * 条码，所有的仓发商品（尖货，自营，假发）会返回这个参数
     */
    @JSONField(name = "barcode")
    private String barcode;







}
