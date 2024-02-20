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
 * 银行账号
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-07-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("bank_account")
public class BankAccountEntity extends BaseEntity<BankAccountEntity> {


    /**
    * 银行账号
    */
    @TableField("bank_account_no")
    private String bankAccountNo;

    /**
    * 账户名称
    */
    @TableField("account_name")
    private String accountName;

    /**
    * 开户银行
    */
    @TableField("bank_type")
    private String bankType;

    /**
     * 金蝶组织机构代码
     */
    @TableField("kindee_org_code")
    private String kindeeOrgCode;

    /**
     * 组织名称
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 组织id
     */
    @TableField("org_id")
    private String orgId;

    /**
     * 启用/禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    public static final String BANK_ACCOUNT_NO = "bank_account_no";

    public static final String ACCOUNT_NAME = "account_name";

    public static final String BANK_TYPE = "bank_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}