package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * <p>
 * 海外仓入库单详情
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_warehouse_inbound_detail")
public class OverseasWarehouseInboundDetailEntity extends BaseEntity<OverseasWarehouseInboundDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 海外仓平台产品名称
    */
    @TableField("platform_product_name")
    private String platformProductName;
    /**
    * 海外仓平台SKU号
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * ERP系统产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * ERP的SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * ERP的SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 是否组合品：combination 组合 single 单品
    */
    @TableField("is_combination")
    private Boolean isCombination;
    /**
    * 签收数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 在途数量
    */
    @TableField("transport_qty")
    private Integer transportQty;
    /**
    * 装箱数量
    */
    @TableField("pack_qty")
    private Integer packQty;
    /**
    * 收发差异
    */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
    * 签收时间
    */
    @TableField("receive_time")
    private LocalDateTime receiveTime;
    /**
    * 签收状态：not=未签收，already=已签收
    */
    @TableField("receive_status")
    private String receiveStatus;
    /**
    * 签收类型：system=平台系统签收，manual=手动签收
    */
    @TableField("receive_type")
    private String receiveType;


    public static final String MAIN_ID = "main_id";

    public static final String PLATFORM_PRODUCT_NAME = "platform_product_name";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String IS_COMBINATION = "is_combination";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String TRANSPORT_QTY = "transport_qty";

    public static final String PACK_QTY = "pack_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String RECEIVE_TIME = "receive_time";

    public static final String RECEIVE_STATUS = "receive_status";

    public static final String RECEIVE_TYPE = "receive_type";

}