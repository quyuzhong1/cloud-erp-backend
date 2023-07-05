package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 手工出入库详情表
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_out_in_stock_detail")
public class DmpOutInStockDetailEntity extends BaseEntity<DmpOutInStockDetailEntity> {


    /**
    * 物料编码
    */
    @TableField("sku_no")
    private String skuNo;

    /**
    * 物料名称
    */
    @TableField("product_name")
    private String productName;

    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;

    /**
    * 仓位
    */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
    * 单价
    */
    @TableField("price")
    private BigDecimal price;

    /**
    * 主表数据id
    */
    @TableField("main_id")
    private String mainId;

    /**
    * 来源详情id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String QTY = "qty";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String PRICE = "price";

    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}