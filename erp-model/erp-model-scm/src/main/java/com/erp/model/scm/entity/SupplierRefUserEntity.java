package com.erp.model.scm.entity;

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
 * 
 * </p>
 *
 * @author zdy
 * @since 2024-01-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("supplier_ref_user")
public class SupplierRefUserEntity extends BaseEntity<SupplierRefUserEntity> {

    /**
    * 用户uid
    */
    @TableField("uid")
    private String uid;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 是否禁用 true 是 false 开启
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
     * 是否超级管理员 false 不是管理员
     */
    @TableField("is_super")
    private Boolean isSuper;

    public static final String UID = "uid";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}