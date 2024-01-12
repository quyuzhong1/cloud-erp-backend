package com.erp.tms.batong.model.order.request;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name = "sku")
    private String skuNo;

    /**
     * 英文品名
     */
    @JSONField(name = "invoice_enname")
    @NotBlank(message = "英文品名不能为空")
    private String invoiceEnName;

    /**
     * 英文品名
     */
    @JSONField(name = "invoice_cnname")
    private String invoiceCnName;


    /**
     * 数量
     */
    @JSONField(name = "invoice_quantity")
    @NotBlank(message = "数量不能为空")
    private String invoiceQuantity;

    /**
     * 单位
     * MTR：米
     * PCE：件
     * SET：套
     * 默认PCE
     */
    @JSONField(name = "unit_code")
    private String unitCode;


    /**
     *单价，2位小数 (1个数量的商品价格)
     */
    @JSONField(name = "invoice_unitcharge")
    @NotBlank(message = "单价不能为空")
    private String invoiceUnitCharge;

    /**
     * 商品单重，2位小数 (1个数量的商品单重)
     */
    @JSONField(name = "net_weight")
    private String netWeight;


    /**
     * 海关协制编号
     */
    @JSONField(name = "hs_code")
    private String hsCode;


    /**
     * 配货信息
     */
    @JSONField(name = "invoice_note")
    private String invoiceNote;

    /**
     * 销售地址
     */
    @JSONField(name = "invoice_url")
    private String invoiceUrl;


    /**
     * 商品图片地址
     */
    @JSONField(name = "invoice_info")
    private String invoiceInfo;



    /**
     * 材质
     */
    @JSONField(name = "invoice_material")
    private String invoiceMaterial;


    /**
     * 规格
     */
    @JSONField(name = "invoice_spec")
    private String invoiceSpec;


    /**
     * 用途
     */
    @JSONField(name = "invoice_use")
    private String invoiceUse;

    /**
     * 品牌
     */
    @JSONField(name = "invoice_brand")
    private String invoiceBrand;

    /**
     * 行邮税号
     */
    @JSONField(name = "posttax_num")
    private String postTaxNum;


}
