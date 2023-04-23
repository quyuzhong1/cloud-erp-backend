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
    public static final String SYNC_KINGDEE_PRODUCT_DETAIL = "${spring.profiles.active}-sync_kingdee_product_detail";
    /**
     * 金蝶物料清单（BOM管理）
     */
    public static final String SYNC_KINGDEE_BOM_INFO = "${spring.profiles.active}-sync_kingdee_bom_info";
    /**
     * 金蝶辅助资料（产品分类、）
     */
    public static final String SYNC_KINGDEE_ASSISTANT_DATA = "${spring.profiles.active}-sync_kingdee_assistant_data";
    /**
     * 金蝶员工（用户管理）
     */
    public static final String SYNC_KINGDEE_SYS_USER_INFO = "${spring.profiles.active}-sync_kingdee_sys_user_info";
    /**
     * 金蝶采购订单（采购订单）
     */
    public static final String SYNC_KINGDEE_PURCHASE_ORDER = "${spring.profiles.active}-sync_kingdee_purchase_order";

    /**
     * 金蝶采购申请单（采购申请单）
     */
    public static final String SYNC_KINGDEE_PURCHASE_APPLICATION_ORDER = "${spring.profiles.active}-sync_kingdee_purchase_application_order";

    /**
     * 店铺变更辅助人
     */
    public static final String SHOP_INFO_CHANGE_CHARGE = "${spring.profiles.active}-shop_info_change_charge";

}
