package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Classname: InventoryHisEntity
 * @Description: 库存历史
 * @CreateTime: 2023-04-27  18:49
 * @Author: zhangchunlin
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("inventory_his")
public class InventoryHisEntity extends BaseEntity<InventoryHisEntity> implements Serializable  {

    /**
     * 实时库存表id
     */
    @TableField("info_id")
    private String infoId;

    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;


    /**
     * 数量；每日结余数量
     */
    @TableField("qty")
    private Integer qty;

    public InventoryHisEntity(String id, Integer curQty) {
        super(id);
        this.qty = curQty;
    }

    public InventoryHisEntity(AtomicReference<Integer> curQty, LocalDate billDate) {
        this.qty = curQty.get();
        this.billDate = billDate;
    }
}