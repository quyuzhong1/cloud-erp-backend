package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 标签信息表
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("label_info")
public class LabelInfoEntity extends BaseEntity<LabelInfoEntity> {

    /**
     * 标签名字
     */
    @TableField("name")
    private String name;

    /**
     * 颜色
     */
    @TableField("color")
    private String color;

    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;


    public static final String NAME = "name";

    public static final String COLOR = "color";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
