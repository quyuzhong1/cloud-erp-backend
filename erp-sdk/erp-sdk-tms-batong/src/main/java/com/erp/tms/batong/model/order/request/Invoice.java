package com.erp.tms.batong.model.order.request;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname Invoice
 * @Description 海关信息
 * @Date 2024-01-12 14:58
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice implements Serializable {

    /**
     * sku
     */
    @Alias("sku")
    private String skuNo;

    /**
     * 英文品名
     */
    @Alias("invoice_enname")
    @NotBlank(message = "英文品名不能为空")
    private String invoiceEnName;

    /**
     * 英文品名
     */
    @Alias("invoice_cnname")
    private String invoiceCnName;


    /**
     * 数量
     */
    @Alias("invoice_quantity")
    @NotBlank(message = "数量不能为空")
    private String invoiceQuantity;

    /**
     * 单位
     * MTR：米
     * PCE：件
     * SET：套
     * 默认PCE
     */
    @Alias("unit_code")
    private String unitCode;


    /**
     *单价，2位小数 (1个数量的商品价格)
     */
    @Alias("invoice_unitcharge")
    @NotBlank(message = "单价不能为空")
    private String invoiceUnitCharge;

    /**
     * 商品单重，2位小数 (1个数量的商品单重)
     */
    @Alias("net_weight")
    private String netWeight;


    /**
     * 海关协制编号
     */
    @Alias("hs_code")
    private String hsCode;


    /**
     * 配货信息
     */
    @Alias("invoice_note")
    private String invoiceNote;

    /**
     * 销售地址
     */
    @Alias("invoice_url")
    private String invoiceUrl;


    /**
     * 商品图片地址
     */
    @Alias("invoice_info")
    private String invoiceInfo;



    /**
     * 材质
     */
    @Alias("invoice_material")
    private String invoiceMaterial;


    /**
     * 规格
     */
    @Alias("invoice_spec")
    private String invoiceSpec;


    /**
     * 用途
     */
    @Alias("invoice_use")
    private String invoiceUse;

    /**
     * 品牌
     */
    @Alias("invoice_brand")
    private String invoiceBrand;

    /**
     * 行邮税号
     */
    @Alias("posttax_num")
    private String postTaxNum;


}
