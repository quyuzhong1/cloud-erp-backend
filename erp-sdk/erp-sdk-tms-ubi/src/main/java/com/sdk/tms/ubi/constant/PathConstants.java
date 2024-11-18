package com.sdk.tms.ubi.constant;

/**
 * @author zdy
 * @ClassName PathConstants

 * @date 2023年10月30日
 * @version: 1.0
 */
public class PathConstants {
    private PathConstants() {
    }

    //测试环境
    public static final  String BASE_URL = "http://qa.etowertech.com";
    //正式环境
    //创建订单
    public static final  String POST_CREATE_ORDERS_URL = "/services/shipper/orders";
    //打印标签
    public static final  String POST_LABELS_URL = "/services/shipper/labels";
    //获取标签
    public static final  String POST_LABEL_SPECS_URL = "/services/shipper/labelSpecs";
    //删除运输订单  系统不允许对已发送的订单进行删除（调用发送货物清单接口后，订单不允许删除）。
    /**
     * 单件删除
     *
     *  /services/shipper/order/{ReferenceNo}
     *
     * /services/shipper/order/{OrderID}
     *
     * URL支持用客户端订单号(referenceNo)或服务端订单号(orderId)来进行删除，如果为一票多件订单需要同时删除，则将客户端订单号(referenceNo)或服务端订单号(orderId)传值主单对应的值。
     */
    public static final  String DELETE_LABEL_SPECS_URL = "/services/shipper/order/{}";
    //获取开通的服务
    public static final  String GET_SERVICE_CATALOG_URL = "/services/shipper/service-catalog";
    //获取跟踪号
    public static final  String POST_TRACK_NUMBER_URL = "/services/shipper/trackingNumbers";
    //发货人扣留订单/释放扣留。interceptOrder
    public static final  String POST_INTERCEPT_ORDER_URL = "/services/shipper/hold";
}
