package com.sdk.oms.wildberries.constant;

/**
 * @author Lambda
 * @Classname ShopifyConstant
 * @Description TODO
 * @Date 2023-08-28 16:43
 * @Created by yl
 */
public interface WildberriesConstant {
    //正式环境
    String TOKEN = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjIwMjUwOTA0djEiLCJ0eXAiOiJKV1QifQ.eyJlbnQiOjEsImV4cCI6MTc3NDAxODg5NSwiaWQiOiIwMTk5NWZlYi04NmJlLTc2YWMtOTIwMy0yM2Q0YjIzYzM5MDEiLCJpaWQiOjI2MTI1NTY1Mywib2lkIjoyNTAwMjY4NDYsInMiOjE2MTI2LCJzaWQiOiIyNmFjYzFkZS1kNzljLTQ3OWEtYTY0Yi1mZjE5YjFlZmI0MTMiLCJ0IjpmYWxzZSwidWlkIjoyNjEyNTU2NTN9.JkOfUEKLJ5AxtWPtse0RxLYFFl4-TJjKYPejF4akGbw6rb_w7SigU4BPtU3OH_f3iknEdUyYsWqnxxpz7GFCDA";

    //检查店铺授权
    String GET_SHOP_CHECK_PING = "https://common-api.wildberries.ru/ping";

    /**
     * 获取平台商品列表
     * Period	Limit	Interval	Burst
     * 1 minute	100 requests	600 milliseconds	5 requests
     */
    String POST_GET_SKU_LIST = "https://content-api.wildberries.ru/content/v2/get/cards/list";
    /**
     * 订单数据查询接口
     * Period	Limit	Interval	Burst
     * 1 minute	300 requests	200 milliseconds	20 requests
     */
    String GET_ORDERS = "https://marketplace-api.wildberries.ru/api/v3/orders";
    /**
     * 订单状态
     * Period	Limit	Interval	Burst
     * 1 minute	300 requests	200 milliseconds	20 requests
     */
    String POST_ORDERS_STATUS = "https://marketplace-api.wildberries.ru/api/v3/orders/status";
    /**
     * 创建组包
     *
     Period	Limit	Interval	Burst
     1 minute	300 requests	200 milliseconds	20 requests
     */
    String POST_CREATE_SUPPLY = "https://marketplace-api.wildberries.ru/api/v3/supplies";
    String POST_ADD_BOX_TO_SUPPLY = "https://marketplace-api.wildberries.ru/api/v3/supplies/{}/trbx";
    String PATCH_ADD_ORDER_TO_SUPPLY = "https://marketplace-api.wildberries.ru/api/v3/supplies/{}/orders/{}";
    //订单面单
    String POST_ORDER_LABEL = "https://marketplace-api.wildberries.ru/api/v3/orders/stickers";
    //标记发货
    String PATCH_SIGN_DELIVERY = "https://marketplace-api.wildberries.ru/api/v3/supplies/{}/deliver";
    //跨境面单
    String POST_CROSS_ORDER_LABEL = "https://marketplace-api.wildberries.ru/api/v3/files/orders/external-stickers";
    //交接单
    String POST_SUPPLY_LABEL = "https://marketplace-api.wildberries.ru/api/v3/supplies/{supplyId}/barcode";
}



