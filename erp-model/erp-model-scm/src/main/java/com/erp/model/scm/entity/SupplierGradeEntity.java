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
 * 供应商等级表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("supplier_grade")
public class SupplierGradeEntity extends BaseEntity<SupplierGradeEntity> {

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 排序
     */
    @TableField("seq")
    private Integer seq;


    public static final String FIELD_NAME = "name";

    public static final String FIELD_SEQ = "seq";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
