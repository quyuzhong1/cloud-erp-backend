package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("order_category_detail")
public class OrderCategoryDetailEntity extends BaseEntity<OrderCategoryDetailEntity> {

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;


    public static final String NAME = "name";

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
