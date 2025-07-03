package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.scm.enums.SupplierVisitEnum;
import com.erp.model.scm.enums.SupplierVisitResultEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

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
@TableName("supplier_visit")
public class
SupplierVisitEntity extends BaseEntity<SupplierVisitEntity> {

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 类型
     */
    @TableField("visit_type")
    private String visitType;

    /**
     * 拜访时间
     */
    @TableField("visit_time")
    private LocalDate visitTime;

    /**
     * 拜访人
     */
    @TableField("people")
    private String people;



    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 结果
     */
    @TableField("result")
    private String result;




    @Override
    public Serializable pkVal() {
        return null;
    }

}
