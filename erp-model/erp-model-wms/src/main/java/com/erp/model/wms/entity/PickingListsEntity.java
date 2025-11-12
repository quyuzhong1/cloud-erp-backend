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
 * 拣货单
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("picking_lists")
public class PickingListsEntity extends BaseEntity<PickingListsEntity> {

    /**
     * 拣货单号
     */
    @TableField("code")
    private String code;

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
     * 来源单据号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 产品总数
     */
    @TableField("sku_total")
    private Integer skuTotal;

    /**
     * 仓位总数
     */
    @TableField("location_total")
    private Integer locationTotal;

    /**
     * 打印人ID
     */
    @TableField("print_user_id")
    private String printUserId;
    /**
     * 打印人姓名
     */
    @TableField("print_user_name")
    private String printUserName;
    /**
     * 打印时间
     */
    @TableField("print_time")
    private String printTime;
    /**
     * 打印状态
     * PackagePrintStatusEnum
     */
    @TableField("print_status")
    private String printStatus;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    public static final String SKU_TOTAL = "sku_total";

    public static final String LOCATION_TOTAL = "location_total";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
