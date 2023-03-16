package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname ProductArchiveEntity
 * @Description TODO
 * @Date 2022-10-09 11:38
 * @Created by yl
 */
@TableName(value ="product_archive")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProductArchiveEntity extends BaseEntity implements Serializable {
    /**
     * 归档时间
     */
    private LocalDateTime archiveTime;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 操作人
     */
    private String operator;
}
