package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品包装信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:05
 **/
@TableName(value ="product_pack")
@Data
public class ProductPackEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 产品尺寸
     */
    @TableField(value = "product_size")
    private String productSize;

    /**
     * 毛重
     */
    @TableField(value = "gross_weight")
    private BigDecimal grossWeight;

    /**
     * 净重
     */
    @TableField(value = "net_weight")
    private BigDecimal netWeight;

    /**
     * 箱规
     */
    @TableField(value = "box_size")
    private String boxSize;

    /**
     * 单箱重量
     */
    @TableField(value = "box_weight")
    private BigDecimal boxWeight;

    /**
     * 单箱数量
     */
    @TableField(value = "box_qty")
    private BigDecimal boxQty;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}