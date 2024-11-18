package com.sdk.tms.yuntu.constants;

public class YunTuConstants {
    private YunTuConstants() {
    }

    //正式环境
    public static final String METHOD_CHANNEL_GET = "/api/Common/GetShippingMethods";
    public static final String METHOD_CREATE_ORDER = "/api/WayBill/CreateOrder";
    public static final String METHOD_GET_TRACKINGNUMBER = "/api/Waybill/GetTrackingNumber";
    public static final String METHOD_PRINT_LABEL = "/api/Label/Print";
    public static final String METHOD_INTERCEPT_ORDER = "/api/WayBill/Intercept";
    public static final String METHOD_CANCEL_ORDER = "/api/WayBill/Delete";
    public static final String METHOD_UPDATE_WEIGHT = "/api/WayBill/UpdateWeight";
}
