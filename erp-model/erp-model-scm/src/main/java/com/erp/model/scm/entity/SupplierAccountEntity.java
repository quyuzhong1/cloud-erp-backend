package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 供应商账户信息
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("supplier_account")
public class SupplierAccountEntity extends BaseEntity<SupplierAccountEntity> {

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;



    /**
     * 收款方
     */
    @TableField("payee")
    private String payee;

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
     * 备注
     */
    @TableField("remark")
    private String remark;


    /**
     * 支行
     */
    @TableField("bank_subbranch")
    private String bankSubbranch;


    /**
     * 支付方式
     */
    @TableField("pay_method_id")
    private String payMethodId;





    @Override
    public Serializable pkVal() {
        return null;
    }

}
