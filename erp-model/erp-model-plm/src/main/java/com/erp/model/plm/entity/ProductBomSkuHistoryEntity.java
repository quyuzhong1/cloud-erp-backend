package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * bom历史表与sku关系表(ProductBomSkuHistory)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_sku_history")
public class ProductBomSkuHistoryEntity implements Serializable {
    private static final long serialVersionUID = -19422095845826564L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 更新人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 父级sku_no '0' 是第一级
     */
    private String parentSkuNo;
    /**
     * sku编号
     */
    private String skuNo;
    /**
     * bom 历史表id
     */
    private String bomHistoryId;


    /**
     * 父级表skuid
     */
    private String parentSkuId;


    /**
     * skuId
     */
    private String skuId;


    /**
     *产品id
     */
    private String productId;
}

