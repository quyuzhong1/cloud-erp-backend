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
 * 订单分类表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("order_category")
public class OrderCategoryEntity extends BaseEntity<OrderCategoryEntity> {

    /**
     * 组名
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 禁用状态false 未禁用
     */
    @TableField("disabled")
    private Boolean disabled;


    public static final String GROUP_NAME = "group_name";

    public static final String REMARK = "remark";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
