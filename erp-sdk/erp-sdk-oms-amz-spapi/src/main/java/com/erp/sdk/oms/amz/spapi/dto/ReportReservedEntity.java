package com.erp.sdk.oms.amz.spapi.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 亚马逊物流预留库存报告 实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReportReservedEntity extends ReportSuperMongoDTO {
    @CsvBindByName(column = "sku")
    private String sku;

    @CsvBindByName(column = "fnsku")
    private String fnsku;

    @CsvBindByName(column = "asin")
    private String asin;

    @CsvBindByName(column = "product-name")
    private String productName;

    @CsvBindByName(column = "reserved_qty")
    private String reservedQty;

    @CsvBindByName(column = "reserved_customerorders")
    private String reservedCustomerOrders;

    @CsvBindByName(column = "reserved_fc-transfers")
    private String reservedFCTransfers;

    @CsvBindByName(column = "reserved_fc-processing")
    private String reservedFCProcessing;

}
