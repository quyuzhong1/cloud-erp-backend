package com.sdk.tms.shopee.constant;

/**
 * @author zdy
 * @ClassName PathConstants
 * @description: TODO
 * @date 2023年10月30日
 * @version: 1.0
 */
public interface PathConstants {

//    String HOST = "https://openplatform.shopee.cn";
    String HOST = "https://partner.test-stable.shopeemobile.com";
    //获取渠道列表
    String GET_CHANNEL_LIST_URL = "/api/v2/logistics/get_channel_list";
    //获取跟踪号码列表
    String GET_TRACK_NUMBER_LIST_URL = "/api/v2/first_mile/get_tracking_number_list";
    //单个获取运单号
    String GET_TRACK_NUMBER_URL = "/api/v2/logistics/get_tracking_number";
    /**
     * 获取发货打印参数
     */
    String POST_GET_SHIPPING_DOCUMENT_PARAMETER = "/api/v2/logistics/get_shipping_document_parameter";
    /**
     * 创建发货记录
     */
    String POST_CREATE_SHIPPING_DOCUMENT = "/api/v2/logistics/create_shipping_document";
    /**
     *获取发货结果
     */
    String POST_GET_SHIPPING_DOCUMENT_RESULT = "/api/v2/logistics/get_shipping_document_result";
    /**
     *下载发货面单
     */
    String POST_DOWNLOAD_SHIPPING_DOCUMENT = "/api/v2/logistics/download_shipping_document";
    /**
     * 获取发货参数
     */
    String GET_SHIPPING_PARAMETER = "/api/v2/logistics/get_shipping_parameter";

    /**
     * 标记发货
     */
    String POST_SHIPPING_ORDER = "/api/v2/logistics/ship_order";

}
