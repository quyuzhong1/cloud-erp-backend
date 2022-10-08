package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品变体属性值表
 * @TableName product_variant_property
 */
@TableName(value ="product_variant_property")
@Data
public class ProductVariantPropertyEntity implements Serializable {

    @ApiModelProperty(value = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty(value = "属性值")
    @TableField(value = "property_value")
    private String propertyValue;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "创建人id")
    @TableField(value = "create_user_id")
    private String createUserId;

    @ApiModelProperty(value = "修改时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "修改人id")
    @TableField(value = "update_user_id")
    private String updateUserId;

    @ApiModelProperty(value = "创建人名称")
    @TableField(value = "create_user_name")
    private String createUserName;

    @ApiModelProperty(value = "修改人名称")
    @TableField(value = "update_user_name")
    private String updateUserName;

    @ApiModelProperty(value = "变体类型表id")
    @TableField(value = "variant_id")
    private String variantId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}