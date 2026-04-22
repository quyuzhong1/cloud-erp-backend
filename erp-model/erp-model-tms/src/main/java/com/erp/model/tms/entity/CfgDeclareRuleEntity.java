package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 报关规则主表
 * </p>
 *
 * @author jack
 * @since 2026-04-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_declare_rule")
public class CfgDeclareRuleEntity extends BaseEntity<CfgDeclareRuleEntity> {

    /**
    * 规则类型: fmDeclareBill=头程报关单, b2bDeclareBill=B2B报关单
    */
    @TableField("rule_type")
    private String ruleType;
    /**
    * 发货人: byCompany=按结算公司 CfgDeclareRuleSenderTypeEnum
    */
    @TableField("sender_type")
    private String senderType;
    /**
    * 发货人
    */
    @TableField("sender_id")
    private String senderId;
    /**
    * 发货人
    */
    @TableField("sender_name")
    private String senderName;
    /**
    * 收货人: byCompany=按结算公司, byCustomer=按客户 CfgDeclareRuleReceiverTypeEnum
    */
    @TableField("receiver_type")
    private String receiverType;
    /**
    * 收货人
    */
    @TableField("receiver_id")
    private String receiverId;
    /**
    * 收货人
    */
    @TableField("receiver_name")
    private String receiverName;
    /**
    * 是否禁用: true=禁用, false=启用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String RULE_TYPE = "rule_type";

    public static final String SENDER_TYPE = "sender_type";

    public static final String SENDER_ID = "sender_id";

    public static final String SENDER_NAME = "sender_name";

    public static final String RECEIVER_TYPE = "receiver_type";

    public static final String RECEIVER_ID = "receiver_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}