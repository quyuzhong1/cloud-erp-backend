package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 供应商拜访表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_supplier_visit")
public class ScmSupplierVisitEntity extends BaseEntity<ScmSupplierVisitEntity> {

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 拜访时间
     */
    @TableField("visit_time")
    private Date visitTime;

    /**
     * 拜访人
     */
    @TableField("people")
    private String people;

    /**
     * skuid
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku 名称
     */
    @TableField("sku_name")
    private String skuName;

    /**
     * 内容
     */
    @TableField("details")
    private String details;

    /**
     * 结果
     */
    @TableField("result")
    private String result;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String TYPE = "type";

    public static final String VISIT_TIME = "visit_time";

    public static final String PEOPLE = "people";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NAME = "sku_name";

    public static final String DETAILS = "details";

    public static final String RESULT = "result";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
