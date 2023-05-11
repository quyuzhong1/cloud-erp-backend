package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 拣货明细
 * </p>
 *
 * @author will
 * @since 2023-05-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("picking_detail")
public class PickingDetailEntity extends BaseEntity<PickingDetailEntity> {

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
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 收货仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 收货仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 仓库组织id
     */
    @TableField("org_id")
    private String orgId;

    /**
     * 仓库组织名称
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 来源单据号
     */
    @TableField("source_code")
    private String sourceCode;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String UNIT = "unit";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String ORG_ID = "org_id";

    public static final String ORG_NAME = "org_name";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOURCE_CODE = "source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
