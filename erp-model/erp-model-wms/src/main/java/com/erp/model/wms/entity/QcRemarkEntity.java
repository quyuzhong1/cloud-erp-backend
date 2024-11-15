package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 质检单备注表
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_remark")
public class QcRemarkEntity extends BaseEntity<QcRemarkEntity> {

    /**
     * 质检单id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}
