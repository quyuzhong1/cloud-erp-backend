package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * FBA调拨发货
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_shipment")
public class DmpShipmentEntity extends BaseEntity<DmpShipmentEntity> {


    /**
    * 申报id
    */
    @TableField("shipp_no")
    private String shippNo;

    /**
    * 批次
    */
    @TableField("batch_no")
    private String batchNo;

    /**
    * 申报名称
    */
    @TableField("shipp_name")
    private String shippName;

    /**
    * 货件状态
    */
    @TableField("shipment_status")
    private String shipmentStatus;

    /**
    * 1:等待发货;2:已发货;3:已签收;4已删除
    */
    @TableField("status")
    private String status;

    /**
    * 发货时间
    */
    @TableField("express_time")
    private String expressTime;

    /**
    * 备注
    */
    @TableField("content")
    private String content;

    /**
    * 发货仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
    * 发货仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
    * 目的仓库id(fba仓库)
    */
    @TableField("fba_warehouse_id")
    private String fbaWarehouseId;

    /**
    * 货件编号
    */
    @TableField("shipment_id")
    private String shipmentId;

    /**
    * 是否完结1完结2未完结
    */
    @TableField("is_over")
    private Integer isOver;

    /**
    * 完结时间
    */
    @TableField("over_time")
    private String overTime;

    /**
    * 城市编码
    */
    @TableField("city_code")
    private String cityCode;

    /**
    * 始发港编码
    */
    @TableField("startport_code")
    private String startportCode;

    /**
    * 目的港编码
    */
    @TableField("endport_code")
    private String endportCode;


    @TableField(exist = false)
    private List<DmpShipmentDetailEntity> itemList;


    public static final String SHIPP_NO = "shipp_no";

    public static final String BATCH_NO = "batch_no";

    public static final String SHIPP_NAME = "shipp_name";

    public static final String SHIPMENT_STATUS = "shipment_status";

    public static final String STATUS = "status";

    public static final String EXPRESS_TIME = "express_time";

    public static final String CONTENT = "content";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String FBA_WAREHOUSE_ID = "fba_warehouse_id";

    public static final String SHIPMENT_ID = "shipment_id";

    public static final String IS_OVER = "is_over";

    public static final String OVER_TIME = "over_time";

    public static final String CITY_CODE = "city_code";

    public static final String STARTPORT_CODE = "startport_code";

    public static final String ENDPORT_CODE = "endport_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}