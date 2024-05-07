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

}
