package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * bom 与sku关系表(BomRefSku)实体类
 *
 * @author yl
 * @since 2023-01-09 11:32:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_sku")
public class BomSkuEntity implements Serializable {
    private static final long serialVersionUID = -92121071004214361L;

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
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 父级id '0' 是第一级
     */
    private String parentSkuNo;
    /**
     * sku
     */
    private String skuNo;
    /**
     * bom 表id
     */
    private String bomId;



}

