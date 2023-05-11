package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Classname: InitStockDetailEntity
 * @Description: TODO
 * @CreateTime: 2023-05-11  10:27
 * @Author: zhangchunlin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("init_stock_detail")
public class InitStockDetailEntity  extends BaseEntity<InitStockDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

}