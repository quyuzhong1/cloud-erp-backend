package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 补货建议标签关系表
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("replenishment_ref_label")
public class ReplenishmentRefLabelEntity extends BaseEntity<ReplenishmentRefLabelEntity> {

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
