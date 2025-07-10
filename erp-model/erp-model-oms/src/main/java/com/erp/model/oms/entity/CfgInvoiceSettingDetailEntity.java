package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 发票设置明细
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_invoice_setting_detail")
public class CfgInvoiceSettingDetailEntity extends BaseEntity<CfgInvoiceSettingDetailEntity> {

    /**
    * 发票设置id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 平台value
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 开票规则：amount=按产品全额开票，custom=按（产品全额×自定义百分比）后开票,deduct=按（产品全额-佣金）后开票  枚举：CfgInvoiceSettingDetailDictInvoiceRuleEnum
    */
    @TableField("dict_invoice_rule")
    private String dictInvoiceRule;
    /**
    * 比例
    */
    @TableField("ratio")
    private BigDecimal ratio;
    /**
    * 是否包含运费
    */
    @TableField("is_contain_ship_fee")
    private Boolean isContainShipFee;
    /**
    * 税费类型：purchase_sale=采购经销，self_sale=自产自销  枚举：CfgInvoiceSettingDetailTaxTypeEnum
    */
    @TableField("tax_type")
    private String taxType;
    /**
    * 开票节点：after_pull=订单拉取后，after_audit=订单审核后，no_auto=不自动开票  枚举：CfgInvoiceSettingDetailInvoiceNodeEnum
    */
    @TableField("invoice_node")
    private String invoiceNode;
    /**
    * 自动上传
    */
    @TableField("is_auto_upload")
    private Boolean isAutoUpload;
    /**
     * 校验类型
     */
    @TableField("dict_verify_type")
    private String dictVerifyType;

    /**
     * 公司token
     */
    @TableField(exist = false)
    private String token;


    public static final String MAIN_ID = "main_id";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SHOP_ID = "shop_id";

    public static final String DICT_INVOICE_RULE = "dict_invoice_rule";

    public static final String RATIO = "ratio";

    public static final String IS_CONTAIN_SHIP_FEE = "is_contain_ship_fee";

    public static final String TAX_TYPE = "tax_type";

    public static final String INVOICE_NODE = "invoice_node";

    public static final String IS_AUTO_UPLOAD = "is_auto_upload";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
