package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 补货建议标签关系表
 * </p>
 *
 * @author will
 * @since 2024-08-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("replenishment_ref_label")
public class ReplenishmentRefLabelEntity extends BaseEntity<ReplenishmentRefLabelEntity> {

    private static final long serialVersionUID = -2436494540475240918L;
    /**
    * 补货建议id
    */
    @TableField("ref_id")
    private String refId;
    /**
    * 标签id
    */
    @TableField("label_id")
    private String labelId;
    /**
    * 类型   补货建议
    */
    @TableField("type")
    private String type;


    public static final String REF_ID = "ref_id";

    public static final String LABEL_ID = "label_id";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}