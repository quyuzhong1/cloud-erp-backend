package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * B2C销售订单分类表
 * </p>
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_ref_category")
public class SoB2cRefCategoryEntity extends BaseEntity<SoB2cRefCategoryEntity> {

    /**
    * b2c销售订单id
    */
    @TableField("so_b2c_id")
        private String soB2cId;
    /**
    * 分类id
    */
    @TableField("category_id")
        private String categoryId;
    /**
    * 分类名称
    */
    @TableField("category_name")
        private String categoryName;


    public static final String SO_B2C_ID = "so_b2c_id";

    public static final String CATEGORY_ID = "category_id";

    public static final String CATEGORY_NAME = "category_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}