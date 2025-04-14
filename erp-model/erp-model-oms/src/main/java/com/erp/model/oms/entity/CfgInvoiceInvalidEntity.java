package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 作废发票号
 * </p>
 *
 * @author hcg
 * @since 2025-04-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_invoice_invalid")
public class CfgInvoiceInvalidEntity extends BaseEntity<CfgInvoiceInvalidEntity> {

    /**
    * 发票设置id
    */
    @TableField("cfg_invoice_setting_id")
    private String cfgInvoiceSettingId;
    /**
    * 序列号
    */
    @TableField("no")
    private String no;
    /**
    * 起始发票号
    */
    @TableField("start_invoice_no")
    private String startInvoiceNo;
    /**
    * 截止发票号
    */
    @TableField("end_invoice_no")
    private String endInvoiceNo;
    /**
    * 作废发票号
    */
    @TableField("deactivate_invoice_no")
    private String deactivateInvoiceNo;
    /**
    * 作废原因
    */
    @TableField("reason")
    private String reason;


    public static final String CFG_INVOICE_SETTING_ID = "cfg_invoice_setting_id";

    public static final String NO = "no";

    public static final String START_INVOICE_NO = "start_invoice_no";

    public static final String END_INVOICE_NO = "end_invoice_no";

    public static final String DEACTIVATE_INVOICE_NO = "deactivate_invoice_no";

    public static final String REASON = "reason";

    @Override
    public Serializable pkVal() {
        return null;
    }

}