package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 仓位移动箱唛明细表
 * <p>
 * 以【箱唛 + SKU + 移出仓位 + 移入仓位】为维度，记录仓位移动操作的箱唛维度明细。
 * 是 {@code warehouse_location_move_detail} 按箱唛拆分后的子表，
 * 主要用于事后查看每次移仓操作中各箱唛的明细数据。
 * </p>
 *
 * @author liuchao
 * @since 2026-05-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_move_carton_detail")
public class WmsMoveCartonDetailEntity extends BaseEntity<WmsMoveCartonDetailEntity> {

    /**
     * 仓位移动主单 ID（warehouse_location_move.id）
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 仓位移动明细 ID（warehouse_location_move_detail.id）
     * <p>
     * 对应汇总后的移动明细行（同一 SKU+移出仓位+移入仓位 汇总出的那条明细）。
     * 多条箱唛明细行可能指向同一个 detail_id（多箱同 SKU 汇总的情况）。
     */
    @TableField("detail_id")
    private String detailId;

    /**
     * 箱唛号
     */
    @TableField("carton_code")
    private String cartonCode;

    /**
     * 装箱单主键（after_sale_pack.id）
     */
    @TableField("carton_id")
    private String cartonId;

    /**
     * 装箱明细主键（after_sale_pack_detail.id）
     */
    @TableField("carton_detail_id")
    private String cartonDetailId;

    /**
     * SKU 主键
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * SKU 编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 移出仓位编码
     */
    @TableField("out_warehouse_location")
    private String outWarehouseLocation;

    /**
     * 移入仓位编码
     */
    @TableField("in_warehouse_location")
    private String inWarehouseLocation;

    /**
     * 本箱该 SKU 的移动数量（装箱明细中的 packQty）
     */
    @TableField("qty")
    private Integer qty;

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
