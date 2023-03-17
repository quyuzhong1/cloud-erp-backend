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
@TableName("supplier_contact")
public class SupplierContactEntity extends BaseEntity<SupplierContactEntity> {

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 联系人名
     */
    @TableField("person")
    private String person;

    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 电话
     */
    @TableField("tel_number")
    private String telNumber;

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




    @Override
    public Serializable pkVal() {
        return null;
    }

}
