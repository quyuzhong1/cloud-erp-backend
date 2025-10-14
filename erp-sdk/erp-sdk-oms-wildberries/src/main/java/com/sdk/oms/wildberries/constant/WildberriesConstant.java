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
    String TOKEN_PRO = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjIwMjUwOTA0djEiLCJ0eXAiOiJKV1QifQ.eyJlbnQiOjEsImV4cCI6MTc3NDAxODg5NSwiaWQiOiIwMTk5NWZlYi04NmJlLTc2YWMtOTIwMy0yM2Q0YjIzYzM5MDEiLCJpaWQiOjI2MTI1NTY1Mywib2lkIjoyNTAwMjY4NDYsInMiOjE2MTI2LCJzaWQiOiIyNmFjYzFkZS1kNzljLTQ3OWEtYTY0Yi1mZjE5YjFlZmI0MTMiLCJ0IjpmYWxzZSwidWlkIjoyNjEyNTU2NTN9.JkOfUEKLJ5AxtWPtse0RxLYFFl4-TJjKYPejF4akGbw6rb_w7SigU4BPtU3OH_f3iknEdUyYsWqnxxpz7GFCDA";
//    String SANDBOX = "";
    //沙箱
    String TOKEN = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjIwMjUwOTA0djEiLCJ0eXAiOiJKV1QifQ.eyJlbnQiOjEsImV4cCI6MTc3NTkzNTU1NiwiaWQiOiIwMTk5ZDIyOS03Njg1LTdhYjAtYTI1Zi0wMmNjYjBhYTgwODUiLCJpaWQiOjI2MTI1NTY1Mywib2lkIjoyNTAwMjY4NDYsInMiOjAsInNpZCI6IjI2YWNjMWRlLWQ3OWMtNDc5YS1hNjRiLWZmMTliMWVmYjQxMyIsInQiOnRydWUsInVpZCI6MjYxMjU1NjUzfQ.ea9b7bGF2-9N7ysITHBdWDfeiFrV2XyCXABCP05BotnsWYvWWUMpEMSw_27pFiJR-PGdnH3XuCQsWQgeSQhYXQ";
    String SANDBOX = "-sandbox";
    //检查店铺授权
    String GET_SHOP_CHECK_PING = "https://common-api.wildberries.ru/ping";
    //产品分类
    String GET_PRODUCT_CATEGORY = "https://content-api" + SANDBOX + ".wildberries.ru/content/v2/object/parent/all";
    //目录
    String GET_PRODUCT_SUBJECT = "https://content-api" + SANDBOX + ".wildberries.ru/content/v2/object/all";
    //产品特征
    String GET_PRODUCT_CHARACTERISTIC = "https://content-api" + SANDBOX + ".wildberries.ru/content/v2/object/charcs/{}";
    //创建产品
    String POST_CREATE_PRODUCT_CARD = "https://content-api" + SANDBOX + ".wildberries.ru/content/v2/cards/upload";
    /**
     * 获取平台商品列表
     * Period	Limit	Interval	Burst
     * 1 minute	100 requests	600 milliseconds	5 requests
     */
    String POST_GET_SKU_LIST = "https://content-api" + SANDBOX + ".wildberries.ru/content/v2/get/cards/list";
    String POST_LIST_PRODUCT_ERROR = "https://content-api.wildberries.ru/content/v2/cards/error/list";
    /**
     * 订单数据查询接口
     * Period	Limit	Interval	Burst
     * 1 minute	300 requests	200 milliseconds	20 requests
     */
    String GET_ORDERS = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/orders";
    String GET_ORDERS_NEW = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/orders/new";
    /**
     * 创建FBS订单
     */
    String POST_CREATE_FBS_ORDER = "https://marketplace-api-sandbox.wildberries.ru/api/v3/test/fbs/orders/make";
    /**
     * 订单状态
     * Period	Limit	Interval	Burst
     * 1 minute	300 requests	200 milliseconds	20 requests
     */
    String POST_ORDERS_STATUS = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/orders/status";
    /**
     * 创建组包
     * <p>
     * Period	Limit	Interval	Burst
     * 1 minute	300 requests	200 milliseconds	20 requests
     */
    String POST_CREATE_SUPPLY = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/supplies";
    String POST_ADD_BOX_TO_SUPPLY = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/supplies/{}/trbx";
    String PATCH_ADD_ORDER_TO_SUPPLY = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/supplies/{}/orders/{}";
    //订单面单
    String POST_ORDER_LABEL = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/orders/stickers";
    //标记发货
    String PATCH_SIGN_DELIVERY = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/supplies/{}/deliver";
    //跨境面单
    String POST_CROSS_ORDER_LABEL = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/files/orders/external-stickers";
    //下载组包交接标签
    String GET_SUPPLY_LABEL = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/supplies/{}/barcode";
    //获取仓库列表
    String GET_WAREHOUSE = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/warehouses";
    //创建仓库
    String CREATE_WAREHOUSE = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/warehouses";
    //更新库存
    String UPDATE_INVENTORY = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/stocks/{}";
    String GET_INVENTORY = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/stocks/{}";
    //获取应用中心
    String GET_OFFICES = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/offices";
    String GET_OFFICES_FOR_PASS = "https://marketplace-api" + SANDBOX + ".wildberries.ru/api/v3/passes/offices";
}



