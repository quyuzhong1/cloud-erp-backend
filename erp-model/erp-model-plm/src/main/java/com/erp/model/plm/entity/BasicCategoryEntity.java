package com.erp.model.plm.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * <p>
 * 产品分类表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("basic_category")
public class BasicCategoryEntity extends BaseEntity<BasicCategoryEntity> implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * 分类名
     */
    @TableField("name")
    private String name;

    /**
     * 分类代码
     */
    @TableField("code")
    private String code;

    /**
     * 父 级id
     */
    @TableField("pid")
    private String pid;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;
}
