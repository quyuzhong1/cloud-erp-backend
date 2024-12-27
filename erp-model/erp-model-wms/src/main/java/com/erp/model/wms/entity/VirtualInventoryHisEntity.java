package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;


/**
 * <p>
 * 虚拟仓库存历史信息
 * </p>
 *
 * @author will
 * @since 2024-12-10
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("virtual_inventory_his")
public class VirtualInventoryHisEntity extends BaseEntity<VirtualInventoryHisEntity> {

    /**
    * 虚拟仓库存id
    */
    @TableField("virtual_inventory_id")
    private String virtualInventoryId;
    /**
    * 快照日期
    */
    @TableField("date")
    private LocalDate date;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;


    public static final String VIRTUAL_INVENTORY_ID = "virtual_inventory_id";

    public static final String DATE = "date";

    public static final String QTY = "qty";

    public VirtualInventoryHisEntity(String id, Integer curQty) {
        super(id);
        this.qty = curQty;
    }

    public VirtualInventoryHisEntity(AtomicReference<Integer> curQty, LocalDate date) {
        this.qty = curQty.get();
        this.date = date;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }


}