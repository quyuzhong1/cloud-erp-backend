package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 多选下拉存储表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("multiple_option")
public class MultipleOptionEntity extends BaseEntity<MultipleOptionEntity> {

    /**
    * 主表id , type字段的表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 单据类型
    */
    @TableField("type")
    private String type;
    /**
    * 下拉值Id
    */
    @TableField("ref_id")
    private String refId;


    public static final String MAIN_ID = "main_id";

    public static final String FIELD_TYPE = "type";

    public static final String REF_ID = "ref_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}