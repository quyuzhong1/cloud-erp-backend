package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品采购信息备注表
 * @TableName product_purchase_remark
 */
@TableName(value ="product_purchase_remark")
@Data
public class ProductPurchaseRemarkEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键id")
    private String id;

    /**
     * 产品采购信息表id
     */
    @TableField(value = "purchase_id")
    @ApiModelProperty(value = "产品采购信息表id")
    private String purchaseId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "update_user_name")
    @ApiModelProperty(value = "修改人名称")
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name")
    @ApiModelProperty(value = "修改人名称")
    private String updateUserName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}