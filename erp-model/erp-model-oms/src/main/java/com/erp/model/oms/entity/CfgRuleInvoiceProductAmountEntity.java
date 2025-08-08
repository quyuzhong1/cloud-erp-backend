package com.erp.model.oms.entity;

import java.math.BigDecimal;
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
 * 发票产品总价计算规则
 * </p>
 *
 * @author zdy
 * @since 2025-07-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_invoice_product_amount")
public class CfgRuleInvoiceProductAmountEntity extends BaseEntity<CfgRuleInvoiceProductAmountEntity> {

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
    * 发票配置id
    */
    @TableField("cfg_id")
    private String cfgId;
    /**
    * 发票规则（Amount：全额，Custom：自定义，扣佣金：Deduct）
     * InvoiceRuleEnum
    */
    @TableField("dict_invoice_rule")
    private String dictInvoiceRule;
    /**
    * 比例（x100）
    */
    @TableField("ratio")
    private BigDecimal ratio;


    public static final String NAME = "name";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String PRIORITY = "priority";

    public static final String CFG_ID = "cfg_id";

    public static final String DICT_INVOICE_RULE = "dict_invoice_rule";

    public static final String RATIO = "ratio";

    @Override
    public Serializable pkVal() {
        return null;
    }

}