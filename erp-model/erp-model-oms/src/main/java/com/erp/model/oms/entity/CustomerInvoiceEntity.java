package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 客户发票信息
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("customer_invoice")
public class CustomerInvoiceEntity extends BaseEntity<CustomerInvoiceEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 发票抬头
     */
    @TableField("head")
    private String head;

    /**
     * 发票类型
     */
    @TableField("type")
    private String type;

    /**
     * 银行名称
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 银行账号
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 是否默认 true 是
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String HEAD = "head";

    public static final String TYPE = "type";

    public static final String BANK_NAME = "bank_name";

    public static final String BANK_ACCOUNT = "bank_account";

    public static final String IS_DEFAULT = "is_default";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
