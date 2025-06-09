package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 基础标签表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("basic_label")
public class BasicLabelEntity extends BaseEntity<BasicLabelEntity> {

    /**
     * 标签名称
     */
    @TableField("name")
    private String name;
    /**
     * 颜色
     */
    @TableField("color")
    private String color;
    /**
     * 标签级别 private 私有，company 公司
     */
    @TableField("level")
    private String level;

    /**
     * 标签级别 private 私有，company 公司
     */
    @TableField("index")
    private Integer index;

    public static final String FIELD_NAME = "name";

    public static final String FIELD_COLOR = "color";

    public static final String FIELD_LEVEL = "level";

    @Override
    public Serializable pkVal() {
        return null;
    }
}