package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 变更管理变更实体的信息表(ProductChangeDetails)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_change_details")
public class ProductChangeDetailsEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -76172530084005528L;

    /**
     * 修改实体的json
     */
    private String detailsJson;
    /**
     * 变更表id
     */
    private String changeInfoId;



}

