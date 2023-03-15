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
 * 供应商表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_supplier")
public class ScmSupplierEntity extends BaseEntity<ScmSupplierEntity> {

    /**
     * 供应商名
     */
    @TableField("name")
    private String name;

    /**
     * 分类名
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 分类id
     */
    @TableField("category_id")
    private String categoryId;

    /**
     * 生命周期
     */
    @TableField("life_cycle")
    private String lifeCycle;

    /**
     * 公司地址
     */
    @TableField("company_address")
    private String companyAddress;

    /**
     * 审核状态 
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 禁用状态 true  禁用 false 
     */
    @TableField("disable_status")
    private Boolean disableStatus;


    public static final String NAME = "name";

    public static final String CATEGORY_NAME = "category_name";

    public static final String CATEGORY_ID = "category_id";

    public static final String LIFE_CYCLE = "life_cycle";

    public static final String COMPANY_ADDRESS = "company_address";

    public static final String AUDIT_STATUS = "audit_status";

    public static final String DISABLE_STATUS = "disable_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
