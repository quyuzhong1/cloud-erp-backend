package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * <p>
 * 直接调拨详情
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
*/
@Data
@Accessors(chain = true)
public class DmpTransferInfoDetailDTO {


    /**
    * 主表id
    */
    private String mainId;

    /**
    * 物料编码
    */
    private String skuNo;

    /**
    * 物料名称
    */
    private String productName;

    /**
    * 单位
    */
    private String unit;

    /**
    * 调拨数量
    */
    private Integer qty;

    /**
    * 来源明细id
    */
    private String sourceDetailId;

    /**
     * 调入仓库code
     */
    private String inWarehouseCode;

    /**
     * 调入仓库名称
     */
    private String inWarehouseName;

    /**
     * 调出仓库code
     */
    private String outWarehouseCode;

    /**
     * 调出仓库名称
     */
    private String outWarehouseName;

    /**
     * 调入库位
     */
    private String inWarehouseLocation;

    /**
     * 调出库位
     */
    private String outWarehouseLocation;

    /**
    * 入库时间
    */
    private LocalDateTime receiveTime;

    /**
    * 入库库存状态编码
    */
    private String inStockStatusCode;

    /**
    * 入库库存状态名称
    */
    private String inStockStatusName;

    /**
    * 出库库存状态编码
    */
    private String outStockStatusCode;

    /**
    * 出库库存状态名称
    */
    private String outStockStatusName;

}