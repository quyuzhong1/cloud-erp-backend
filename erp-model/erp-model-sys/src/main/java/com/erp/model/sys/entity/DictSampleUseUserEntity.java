package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 示例用户 字典表
 * </p>
 *
 * @author Lambda
 * @since 2025-01-27
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_sample_use_user")
@EqualsAndHashCode
public class DictSampleUseUserEntity extends BaseEntity<DictSampleUseUserEntity> {

    /**
     * 用户名称
     */
    @TableField("name")
    private String name;

    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    public static final String FIELD_NAME = "name";

    public static final String FIELD_DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
