package com.erp.oms.aliexpress.constants;

/**
 *api name
 *
 *@author yl
 *@date 2023-11-22
 */
public interface AliexpressConstants {
    
    String TOKEN_CREATE = "/auth/token/create";


    String SUCCESS_CODE="0";
    
    
    String LIST_ORDER="aliexpress.trade.seller.orderlist.get";

    /**
     * 订单详情
     */
    String ORDER_DETAIL="aliexpress.trade.new.redefining.findorderbyid";

    Integer pageSize=50;
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

}
