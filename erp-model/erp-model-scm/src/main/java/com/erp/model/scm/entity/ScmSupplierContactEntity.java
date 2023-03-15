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
 * 供应商联系人表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_supplier_contact")
public class ScmSupplierContactEntity extends BaseEntity<ScmSupplierContactEntity> {

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 联系人名
     */
    @TableField("name")
    private String name;

    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 是否默认 true  是
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 开启状态 true 开启
     */
    @TableField("open_status")
    private Boolean openStatus;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String NAME = "name";

    public static final String POSITION = "position";

    public static final String PHONE = "phone";

    public static final String EMAIL = "email";

    public static final String IS_DEFAULT = "is_default";

    public static final String OPEN_STATUS = "open_status";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
