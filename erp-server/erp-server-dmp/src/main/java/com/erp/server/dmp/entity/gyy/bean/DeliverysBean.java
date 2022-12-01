package com.erp.server.dmp.entity.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class DeliverysBean {
    /**
     * delivery : true
     * code : SDO546455586734
     * printExpress : true
     * printDeliveryList : false
     * scan : true
     * weight : false
     * warehouse_name : B2C天猫京东仓
     * warehouse_code : CK062
     * express_name : 中通-菜鸟面单
     * express_code : ZTCN
     * mail_no : 78632828292719
     */

    @SerializedName("delivery")
    private Boolean delivery;
    @SerializedName("code")
    private String code;
    @SerializedName("printExpress")
    private Boolean printExpress;
    @SerializedName("printDeliveryList")
    private Boolean printDeliveryList;
    @SerializedName("scan")
    private Boolean scan;
    @SerializedName("weight")
    private Boolean weight;
    @SerializedName("warehouse_name")
    private String warehouseName;
    @SerializedName("warehouse_code")
    private String warehouseCode;
    @SerializedName("express_name")
    private String expressName;
    @SerializedName("express_code")
    private String expressCode;
    @SerializedName("mail_no")
    private String mailNo;
}
