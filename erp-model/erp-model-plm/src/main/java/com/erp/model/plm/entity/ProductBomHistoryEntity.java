package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * bom 历史表(ProductBomHistory)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_history")
public class ProductBomHistoryEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 618109019196556864L;

    /**
     * 编号
     */
    private String serialNumber;

    /**
     * 类型combination 组合 single 单品
     */
    private String type;

    /**
     * bom 表id
     */
    private String bomId;


}

