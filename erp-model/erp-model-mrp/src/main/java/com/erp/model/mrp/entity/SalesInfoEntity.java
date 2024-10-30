package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 历史销量信息
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sales_info")
public class SalesInfoEntity extends BaseEntity<SalesInfoEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 日期
     */
    @TableField("date")
    private LocalDate date;

    /**
     * 销量
     */
    @TableField("sales_qty")
    private BigDecimal salesQty;

    /**
     * 断货数据是否从历史销量中排除,true是，false否
     */
    @TableField("is_ignore_out_of_stock")
    private Boolean isIgnoreOutOfStock;

    /**
     * sales_qty_type
     */
    @TableField("sales_qty_type")
    private String salesQtyType;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String DATE = "date";

    public static final String SALES_QTY = "sales_qty";

    public static final String IS_IGNORE_OUT_OF_STOCK = "is_ignore_out_of_stock";

    public static final String SALES_QTY_TYPE = "sales_qty_type";

    public static final String ORIGINAL_SALES_QTY = "original_sales_qty";

    public static final String ORIGINAL_INVENTORY_QTY = "original_inventory_qty";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
