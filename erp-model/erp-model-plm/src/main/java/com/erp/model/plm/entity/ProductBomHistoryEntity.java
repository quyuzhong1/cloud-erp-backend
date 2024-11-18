package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * bom 历史表(ProductBomHistory)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_history")
public class ProductBomHistoryEntity extends BaseEntity<ProductBomHistoryEntity> implements Serializable {
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

    /**
     * bom版本
     */
    private String bomVersion;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;
}

