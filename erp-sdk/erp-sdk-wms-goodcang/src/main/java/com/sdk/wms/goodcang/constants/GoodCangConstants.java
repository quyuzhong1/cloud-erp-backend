package com.sdk.wms.goodcang.constants;

public class GoodCangConstants {
    private GoodCangConstants() {
        throw new IllegalStateException("Utility GoodCangConstants class");
    }
    public static final String BASE_URL = "https://uat-oms.eminxing.com";
    public static final String BASE_URL_PROD = "https://oms.goodcang.net";
    public static final String METHOD_GET_SKU_LIST = "/public_open/product/get_product_sku_list";
    public static final String METHOD_GET_WAREHOUSE = "/public_open/base_data/get_warehouse";
    public static final String METHOD_GET_RECEIPT_BATCH = "/public_open/inbound_order/get_receipt_batch";
    public static final String METHOD_GET_SMCODE_TWC_TO_WAREHOUSE = "/public_open/inbound_order/get_smcode_twc_to_warehouse";
    public static final String METHOD_GET_CREATE_INBOUND_BILL = "/public_open/inbound_order/create_grn";
    public static final String METHOD_GET_EDIT_INBOUND_BILL = "/public_open/inbound_order/modify_grn";
    public static final String METHOD_GET_CANCEL_INBOUND_BILL = "/public_open/inbound_order/del_grn";
    public static final String METHOD_GET_CREATE_OUTBOUND_BILL = "/public_open/order/create_order";
    public static final String METHOD_GET_CANCEL_OUTBOUND_BILL = "/public_open/order/cancel_order";
    public static final String METHOD_GET_PRODUCT_INVENTORY = "/public_open/inventory/get_product_inventory";
    public static final String METHOD_GET_SHIPPING_METHOD = "/public_open/base_data/get_shipping_method";
    public static final String METHOD_GET_ORDER_LIST = "/public_open/order/get_order_list";
    public static final String METHOD_GET_GRN_DETAIL = "/public_open/inbound_order/get_grn_detail";
    public static final String METHOD_GET_OUT_BOUND_CODE = "/public_open/order/get_order_by_ref_code";
    public static final String METHOD_GET_RETURN_INSTOCK = "/public_open/return_order/list";
}
