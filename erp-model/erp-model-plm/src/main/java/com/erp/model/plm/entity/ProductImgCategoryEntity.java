package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 图片分类表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("product_img_category")
public class ProductImgCategoryEntity extends BaseEntity<ProductImgCategoryEntity> {

    /**
    * 分类名称
    */
    @TableField("name")
    private String name;
    /**
    * 父级分类ID，空字符串表示根节点
    */
    @TableField("parent_id")
    private String parentId;
    /**
    * 第几级分类（避免递归计算），1表示第一级
    */
    @TableField("level")
    private Integer level;
    /**
    * 是否是系统自带，系统自带的分类不允许删除
    */
    @TableField("is_system")
    private Boolean isSystem;
    /**
    * 排序字段，用于同级分类的排序
    */
    @TableField("sort")
    private Integer sort;


    public static final String NAME = "name";

    public static final String PARENT_ID = "parent_id";

    public static final String LEVEL = "level";

    public static final String IS_SYSTEM = "is_system";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}