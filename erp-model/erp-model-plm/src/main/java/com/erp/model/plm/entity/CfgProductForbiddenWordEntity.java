package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 产品违禁词库
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_product_forbidden_word")
public class CfgProductForbiddenWordEntity extends BaseEntity<CfgProductForbiddenWordEntity> {

    /**
     * 违禁词
     */
    @TableField("forbidden_word")
    private String forbiddenWord;

    /**
     * 是否禁用：false=启用，true=禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    public static final String FORBIDDEN_WORD = "forbidden_word";

    public static final String DISABLED = "disabled";
}
