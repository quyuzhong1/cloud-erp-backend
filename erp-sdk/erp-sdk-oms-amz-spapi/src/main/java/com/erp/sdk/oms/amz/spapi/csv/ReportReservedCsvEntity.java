package com.erp.sdk.oms.amz.spapi.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


/**
 * 亚马逊物流预留库存报告 实体
 */
@Data
@NoArgsConstructor
public class ReportReservedCsvEntity {
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

    public Integer reservedFCTransfersCheckToInt(){
        if (StringUtils.isBlank(this.reservedFCTransfers)){
            return 0;
        }
        return Integer.parseInt(this.reservedFCTransfers);
    }

    public Integer reservedFCProcessingCheckToInt(){
        if (StringUtils.isBlank(this.reservedFCProcessing)){
            return 0;
        }
        return Integer.parseInt(this.reservedFCProcessing);
    }

    public Integer reservedCustomerOrdersCheckToInt(){
        if (StringUtils.isBlank(this.reservedCustomerOrders)){
            return 0;
        }
        return Integer.parseInt(this.reservedCustomerOrders);
    }
}
