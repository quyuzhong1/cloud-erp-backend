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
 * @Classname: InventoryHisEntity
 * @Description: 库存历史
 * @CreateTime: 2023-04-27  18:49
 * @Author: zhangchunlin
 */
@Getter
@Setter
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

}