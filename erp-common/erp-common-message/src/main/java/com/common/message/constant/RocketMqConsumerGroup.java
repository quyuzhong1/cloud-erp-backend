package com.common.message.constant;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/10 11:56
 */
public class RocketMqConsumerGroup {

    /**
     * 金蝶物料（产品信息）
     */
    public static final String SYNC_KINGDEE_PRODUCT_DETAIL = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_product_detail";
    /**
     * 金蝶物料清单（BOM管理）
     */
    public static final String SYNC_KINGDEE_BOM_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_bom_info";
    /**
     * 金蝶辅助资料（产品分类、）
     */
    public static final String SYNC_KINGDEE_ASSISTANT_DATA = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_assistant_data";
    /**
     * 金蝶员工（用户管理）
     */
    public static final String SYNC_KINGDEE_SYS_USER_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_sys_user_info";
    /**
     * 金蝶部门（部门管理）
     */
    public static final String SYNC_KINGDEE_SYS_DEPARTMENT = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_sys_department";
    /**
     * 金蝶采购订单（采购订单）
     */
    public static final String SYNC_KINGDEE_PURCHASE_ORDER = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_order";

    /**
     * 金蝶采购价目表（采购价目表）
     */
    public static final String SYNC_KINGDEE_PURCHASE_PRICE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_price";

    /**
     * 金蝶采购调价表（采购调价表）
     */
    public static final String SYNC_KINGDEE_PURCHASE_PRICE_CHANGE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_price_change";

    /**
     * 金蝶退货单
     */
    public static final String SYNC_KINGDEE_PURCHASE_RETURN_ORDER = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_return_order";

    /**
     * 金蝶仓库
     */
    public static final String SYNC_KINGDEE_WAREHOUSE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_warehouse";

    /**
     * 金蝶供应商
     */
    public static final String SYNC_KINGDEE_SUPPLIER = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_supplier";

    /**
     * 金蝶入库单
     */
    public static final String SYNC_KINGDEE_PURCHASE_STOCK_IN = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_stock_in";

    /**
     * 金蝶直接调拨单
     */
    public static final String SYNC_KINGDEE_TRANSFER_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_transfer_info";

    /**
     * 客户列表
     */
    public static final String SYNC_KINGDEE_CUSTOMER_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_customer_info";

    /**
     * 客户分组
     */
    public static final String SYNC_KINGDEE_CUSTOMER_GROUP = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_customer_group";

    /**
     * 客户分组
     */
    public static final String SYNC_KINGDEE_CUSTOMER_CONTACT = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_customer_contact";

    /**
     * 其他出库
     */
    public static final String SYNC_KINGDEE_OTHER_OUTSTOCK = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_other_outstock";

    /**
     * 其他入库
     */
    public static final String SYNC_KINGDEE_OTHER_INSTOCK = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_other_instock";

    /**
     * 加工单
     */
    public static final String SYNC_KINGDEE_MACHINE_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_machine_info";

    /**
     * 销售订单
     */
    public static final String SYNC_KINGDEE_SO_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_info";

    /**
     * 销售订单变更
     */
    public static final String SYNC_KINGDEE_SO_CHANGE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_change";

    /**
     * 销售出库
     */
    public static final String SYNC_KINGDEE_SO_OUTSTOCK = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_outstock";

    /**
     * 销售退货单
     */
    public static final String SYNC_KINGDEE_SO_RETURN = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_return";

    //-----------------------------------------------------------------dmp数据更新------------------------------------------------------

    /**
     * 店铺变更负责人
     */
    public static final String SHOP_INFO_CHANGE_CHARGE = "${spring.cloud.nacos.discovery.namespace}-shop_info_change_charge";

    /**
     * 店铺变更部门
     */
    public static final String SHOP_INFO_CHANGE_DEPT = "${spring.cloud.nacos.discovery.namespace}-shop_info_change_dept";

    /**
     * 汇率变更
     */
    public static final String CHANGE_CURRENCY = "${spring.cloud.nacos.discovery.namespace}-change_currency";

    /**
     * 产品上市时间
     */
    public static final String PRODUCT_LISTING_UPDATE = "${spring.cloud.nacos.discovery.namespace}-product_listing_update";



}
