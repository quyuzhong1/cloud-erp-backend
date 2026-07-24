package com.erp.oms.aliexpress.constants;

/**
 *api name
 *
 *@author yl
 *@date 2023-11-22
 */
public interface AliexpressConstants {
    String BASE_URL = "https://api-sg.aliexpress.com";

    String TOKEN_CREATE = "/auth/token/create";

    /**
     * 刷新token
     */
    String REFRESH_TOKEN = "/auth/token/refresh";


    String SUCCESS_CODE="0";
    
    
    String LIST_ORDER="aliexpress.trade.seller.orderlist.get";

    String ADDRESS ="aliexpress.trade.seller.order.decrypt";

    /**
     * 订单详情
     */
    String ORDER_DETAIL="aliexpress.trade.new.redefining.findorderbyid";

    Integer pageSize=20;
    /**
     * 产品分页的 api name
     */
    String LIST_PRODUCT="aliexpress.postproduct.redefining.findproductinfolistquery";

    /**
     * 产品详情的 api name
     */
    String PRODUCT_INFO="aliexpress.postproduct.redefining.findaeproductbyid";


    /**
     *上架
     */
    String ON_SELLING="onSelling";


    /**
     *声明发货
     */
    String DECLARE_DELIVER="aliexpress.logistics.sellershipmentfortop";

    /**
     *   子单声明发货
     */
    String SUB_DECLARE_DELIVER = "aliexpress.logistics.order.shipment";

    /**
     * 海外托管标发平台
     */
    String OVERSEAS_MANAGED_SUB_DECLARE_DELIVER = "aliexpress.asf.local.supply.sub.declareship";

    /**
     * 查询海外托管卖家关系
     */
    String SELLER_RELATION_QUERY = "global.seller.relation.query";

    /**
     * 查询速卖通货品关系
     */
    String ASCP_ITEM_QUERY = "aliexpress.ascp.item.query";

    /**
     * 查询速卖通官方仓库存流水
     */
    String AIC_INVENTORY_LOG_QUERY = "global.merchant.aic.invLog";

    /**
     * 全托管店铺卖家关系业务类型
     */
    String ONE_STOP_SERVICE = "ONE_STOP_SERVICE";

    /**
     * 全托管货品业务租户
     */
    Integer FULL_MANAGED_ITEM_BIZ_TYPE = 5110000;

    /**
     * 海外仓表示
     */
    String CAINIAO_INTERNATIONAL_WAREHOUSE="cainiaoInternationalWarehouse";

    /**
     * 发货单查询
     */
    String ALIEXPRESS_ASCP_FFO_QUERY="aliexpress.ascp.ffo.query";

    /**
     * 发货单明细查询
     */
    String ALIEXPRESS_ASCP_FFO_ITEM_QUERY="aliexpress.ascp.ffo.item.query";

    /**
     * 产品明细查询
     */
    String ALIEXPRESS_OFFER_PRODUCT_QUERY="aliexpress.offer.product.query";


    /**
     * 查询卖家资料
     */
    String ALIEXPRESS_MERCHANT_PROFILE_GET ="aliexpress.merchant.profile.get";


    /**
     * 查询所有的实际承运商
     */
    String ALIEXPRESS_CARRIER_QUERY_LIST = "cainiao.global.logistics.carrier.querylist";

}
