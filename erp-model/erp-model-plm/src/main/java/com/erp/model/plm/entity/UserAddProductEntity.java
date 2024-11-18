package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户收藏产品表
 * @TableName user_add_product
 */
@TableName(value ="user_add_product")
@Data
public class UserAddProductEntity extends BaseEntity<UserAddProductEntity> implements Serializable {

    /**
     * 产品id
     */
    private String productId;

    /**
     * 用户id
     */
    private String userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}