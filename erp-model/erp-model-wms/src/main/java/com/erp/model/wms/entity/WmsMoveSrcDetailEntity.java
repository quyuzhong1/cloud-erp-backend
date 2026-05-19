package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 仓位移动来源明细表
 * <p>
 * 以【来源编号 + SKU + 移出仓位 + 移入仓位】为维度，记录仓位移动操作的原始来源明细。
 * 是 {@code warehouse_location_move_detail} 按来源单据拆分后的子表，
 * 主要用于事后查看每次移仓操作中各来源单据的明细数据。
 * </p>
 *
 * @author liuchao
 * @since 2026-05-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_move_src_detail")
public class WmsMoveSrcDetailEntity extends BaseEntity<WmsMoveSrcDetailEntity> {

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
     * 来源单据编号（如箱唛号、其他来源单号等）
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源单据主键（如装箱单 ID、其他来源单 ID 等）
     */
    @TableField("source_id")
    private String sourceId;

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
