package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 产品信息审核意见表
 * @date 2022/11/28 15:50
 */
@TableName(value ="product_detail_comment")
@Data
public class ProductDetailCommentEntity extends BaseEntity implements Serializable {

    /**
     * 产品信息id
     */
    @TableField("product_detail_id")
    private String productDetailId;

    /**
     * 内容
     */
    @TableField("comment")
    private String comment;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
