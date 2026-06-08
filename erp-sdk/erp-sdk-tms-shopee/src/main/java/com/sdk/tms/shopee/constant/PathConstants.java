package com.sdk.tms.shopee.constant;

/**
 * @author zdy
 * @ClassName PathConstants

 * @date 2023年10月30日
 * @version: 1.0
 */
public class PathConstants {

    private PathConstants(){}

    public static final String HOST = "https://partner.test-stable.shopeemobile.com";
    //获取渠道列表
    public static final String GET_CHANNEL_LIST_URL = "/api/v2/logistics/get_channel_list";
    //获取快递服务公司
    public static final String GET_COURIER_DELIVERY_CHANNEL_LIST_URL = "/api/v2/first_mile/get_courier_delivery_channel_list";
    //获取Shopee转运仓信息
    public static final String GET_TRANSIT_WAREHOUSE_LIST_URL = "/api/v2/first_mile/get_transit_warehouse_list";
    //给快递公司下单并绑定组包揽收批次号
    public static final String POST_GENERATE_AND_BIND_FIRST_MILE_TRACKING_NUMBER_URL = "/api/v2/first_mile/generate_and_bind_first_mile_tracking_number";
    //获取快递寄送模式面单
    public static final String POST_GET_COURIER_DELIVERY_WAYBILL_URL = "/api/v2/first_mile/get_courier_delivery_waybill";
    //解绑订单头程追踪号或绑定ID
    public static final String POST_UNBIND_FIRST_MILE_TRACKING_NUMBER_ALL_URL = "/api/v2/first_mile/unbind_first_mile_tracking_number_all";
    //解绑指定头程追踪号
    public static final String POST_UNBIND_FIRST_MILE_TRACKING_NUMBER_URL = "/api/v2/first_mile/unbind_first_mile_tracking_number";
    //生成头程追踪号
    public static final String POST_GENERATE_FIRST_MILE_TRACKING_NUMBER_URL = "/api/v2/first_mile/generate_first_mile_tracking_number";
    //绑定头程追踪号
    public static final String POST_BIND_FIRST_MILE_TRACKING_NUMBER_URL = "/api/v2/first_mile/bind_first_mile_tracking_number";
    //获取头程面单文件
    public static final String POST_GET_FIRST_MILE_WAYBILL_URL = "/api/v2/first_mile/get_waybill";
    //获取头程物流渠道列表
    public static final String GET_FIRST_MILE_CHANNEL_LIST_URL = "/api/v2/first_mile/get_channel_list";
    //获取跟踪号码列表
    public static final String GET_TRACK_NUMBER_LIST_URL = "/api/v2/first_mile/get_tracking_number_list";
    //单个获取运单号
    public static final String GET_TRACK_NUMBER_URL = "/api/v2/logistics/get_tracking_number";
    //获取卖家设置的地址
    public static final String GET_ADDRESS_LIST_URL = "/api/v2/logistics/get_address_list";
    /**
     * 获取发货打印参数
     */
    public static final String POST_GET_SHIPPING_DOCUMENT_PARAMETER = "/api/v2/logistics/get_shipping_document_parameter";
    /**
     * 创建发货记录
     */
    public static final String POST_CREATE_SHIPPING_DOCUMENT = "/api/v2/logistics/create_shipping_document";
    /**
     *获取发货结果
     */
    public static final String POST_GET_SHIPPING_DOCUMENT_RESULT = "/api/v2/logistics/get_shipping_document_result";
    /**
     *下载发货面单
     */
    public static final String POST_DOWNLOAD_SHIPPING_DOCUMENT = "/api/v2/logistics/download_shipping_document";
    /**
     * 获取发货参数
     */
    public static final String GET_SHIPPING_PARAMETER = "/api/v2/logistics/get_shipping_parameter";

    /**
     * 标记发货
     */
    public static final String POST_SHIPPING_ORDER = "/api/v2/logistics/ship_order";
    /**
     * 获取卖家设置的月结账号
     */
    public static final String GET_MERCHANT_PREPAID_ACCOUNT_LIST_URL = "/api/v2/merchant/get_merchant_prepaid_account_list";

}
