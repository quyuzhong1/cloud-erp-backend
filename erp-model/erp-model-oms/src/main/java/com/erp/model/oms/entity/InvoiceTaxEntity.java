package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 发票税务信息
 * </p>
 *
 * @author will
 * @since 2025-04-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("invoice_tax")
public class InvoiceTaxEntity extends BaseEntity<InvoiceTaxEntity> {

    /**
    * listing表id
    */
    @TableField("listing_id")
    private String listingId;
    /**
    * 发票海关编码
    */
    @TableField("invoice_hs_code")
    private String invoiceHsCode;
    /**
    * 单位
    */
    @TableField("unit")
    private String unit;
    /**
    * 跨州税务编码
    */
    @TableField("diff_state_tax_code")
    private String diffStateTaxCode;
    /**
    * 同州税务编码
    */
    @TableField("same_state_tax_code")
    private String sameStateTaxCode;
    /**
    * 原产地
    */
    @TableField("dict_origin")
    private String dictOrigin;
    /**
    * 开票产品名称
    */
    @TableField("invoice_product_name")
    private String invoiceProductName;
    /**
    * 类型，（invoiceType字典）
    */
    @TableField("type")
    private String type;


    /**
     * 平台SKU
     */
    @TableField(exist = false)
    private String platformSkuNo;

    /**
     * 平台
     */
    @TableField(exist = false)
    private String platform;

    /**
     * 店铺Id
     */
    @TableField(exist = false)
    private String shopId;

    public static final String LISTING_ID = "listing_id";

    public static final String INVOICE_HS_CODE = "invoice_hs_code";

    public static final String UNIT = "unit";

    public static final String DIFF_STATE_TAX_CODE = "diff_state_tax_code";

    public static final String SAME_STATE_TAX_CODE = "same_state_tax_code";

    public static final String DICT_ORIGIN = "dict_origin";

    public static final String INVOICE_PRODUCT_NAME = "invoice_product_name";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}