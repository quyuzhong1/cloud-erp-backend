package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 开票规则
 * </p>
 *
 * @author zdy
 * @since 2025-05-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_invoice")
public class CfgRuleInvoiceEntity extends BaseEntity<CfgRuleInvoiceEntity> {

    /**
    * 规则名称
    */
    @TableField("name")
    private String name;
    /**
    * 禁用状态 false 未禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 描述
    */
    @TableField("remark")
    private String remark;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 发票类型:vat=VAT发票,nfe=NF-e发票  枚举：CfgRuleInvoiceInvoiceTypeEnum
    */
    @TableField("invoice_type")
    private String invoiceType;


    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String PRIORITY = "priority";

    public static final String INVOICE_TYPE = "invoice_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
