package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: InventoryDetailEntity
 * @Description: 库存明细
 * @CreateTime: 2023-04-25  18:49
 * @Author: zhangchunlin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("inventory_detail")
public class InventoryDetailEntity extends BaseEntity<InventoryDetailEntity> implements Serializable  {

    /**
     * 实时库存表id
     */
    @TableField("info_id")
    private String infoId;

    /**
     * 批次日期
     */
    @TableField("instock_batch_date")
    private LocalDate instockBatchDate;


    /**
     * 数量。
     * 入库时根据仓库+组织+仓位+SKU+状态+批次日期增加行记录；
     * 出库时根据根据仓库+组织+仓位+SKU+状态+批次日期扣减数量
     */
    @TableField("qty")
    private Integer qty;

}