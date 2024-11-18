package com.sdk.wms.iml.constants;

public class ImlConstants {
    private ImlConstants() {
        throw new IllegalStateException("Utility ImlConstants class");
    }
    public static final String METHOD_GET_PRODUCT_LIST = "getProductList";

    public static final String METHOD_GET_WAREHOUSE = "getWarehouse";

    public static final String METHOD_GET_RECEIVING_REGION = "getRegionForReceiving";

    public static final String METHOD_GET_RECEIPT = "getAsnList";

    public static final String METHOD_CREATE_INBOUND = "createAsn";

    public static final String METHOD_EDIT_INBOUND = "modifyAsn";

    public static final String METHOD_CANCEL_INBOUND = "cancelAsn";

    public static final String METHOD_CREATE_ORDER = "createOrder";

    public static final String METHOD_CANCEL_ORDER = "cancelOrder";

    public static final String METHOD_GET_PRODUCT_INVENTORY = "getProductInventory";

    public static final String GET_SHIPPING_METHOD = "getShippingMethod";

    public static final String GET_ORDER_LIST = "getOrderList";
}
