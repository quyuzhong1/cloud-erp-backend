package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class DeliveryStatusInfoBean {
    /**
     * scan : false
     * weight : false
     * wms : 0
     * delivery : 0
     * cancel : false
     * intercept : false
     * print_express : false
     * express_print_name : null
     * express_print_date : null
     * print_delivery : false
     * delivery_print_name : null
     * delivery_print_date : null
     * scan_name : null
     * scan_date : null
     * weight_name : null
     * weight_date : null
     * wms_order : 0
     * delivery_name : null
     * delivery_date : null
     * cancel_name : null
     * cancel_date : null
     * weight_qty : 0.0
     * thermal_print : 1
     * thermal_print_status : -1
     * picking_user : null
     * picking_date : null
     * standard_weight : 0.147
     * pick_finish : false
     * logistics_printed_bitch : null
     * logistics_serial_no : null
     * wms_date : null
     * volume_total : null
     */

    @SerializedName("scan")
    private boolean scan;
    @SerializedName("weight")
    private boolean weight;
    @SerializedName("wms")
    private int wms;
    @SerializedName("delivery")
    private int delivery;
    @SerializedName("cancel")
    private boolean cancel;
    @SerializedName("intercept")
    private boolean intercept;
    @SerializedName("print_express")
    private boolean printExpress;
    @SerializedName("express_print_name")
    private Object expressPrintName;
    @SerializedName("express_print_date")
    private Object expressPrintDate;
    @SerializedName("print_delivery")
    private boolean printDelivery;
    @SerializedName("delivery_print_name")
    private Object deliveryPrintName;
    @SerializedName("delivery_print_date")
    private Object deliveryPrintDate;
    @SerializedName("scan_name")
    private Object scanName;
    @SerializedName("scan_date")
    private Object scanDate;
    @SerializedName("weight_name")
    private Object weightName;
    @SerializedName("weight_date")
    private Object weightDate;
    @SerializedName("wms_order")
    private int wmsOrder;
    @SerializedName("delivery_name")
    private String deliveryName;
    @SerializedName("delivery_date")
    private String deliveryDate;
    @SerializedName("cancel_name")
    private Object cancelName;
    @SerializedName("cancel_date")
    private Object cancelDate;
    @SerializedName("weight_qty")
    private String weightQty;
    @SerializedName("thermal_print")
    private int thermalPrint;
    @SerializedName("thermal_print_status")
    private int thermalPrintStatus;
    @SerializedName("picking_user")
    private Object pickingUser;
    @SerializedName("picking_date")
    private Object pickingDate;
    @SerializedName("standard_weight")
    private double standardWeight;
    @SerializedName("pick_finish")
    private boolean pickFinish;
    @SerializedName("logistics_printed_bitch")
    private Object logisticsPrintedBitch;
    @SerializedName("logistics_serial_no")
    private Object logisticsSerialNo;
    @SerializedName("wms_date")
    private Object wmsDate;
    @SerializedName("volume_total")
    private Object volumeTotal;

}
