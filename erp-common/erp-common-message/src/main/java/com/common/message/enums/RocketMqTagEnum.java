package com.common.message.enums;

import cn.hutool.core.collection.CollectionUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp topic 使用 RocketMqTopic.DMP_ERP_ORDER_TOPIC
 * tag 区分不同类型数据方便订阅
 * tag 统一归集配置常量 避免不同业务发生冲突
 *
 * @Author Cloud
 * @Date 2023/2/6 10:32
 **/
public enum RocketMqTagEnum {
    /**
     * 管易销售订单tag
     */
    GYY_SALE_ORDER_TAG(1,RocketMqTagEnum.SALES_ORDER,"gyy_sales_order_tag"),
    /**
     * 管易销售历史订单tag
     */

    GYY_SALE_HISTORY_ORDER_TAG(2,RocketMqTagEnum.SALES_ORDER,"gyy_sales_history_order_tag"),
    /**
     * 管易发货订单tag
     */
    GYY_DELIVERY_ORDER_TAG(3,RocketMqTagEnum.DELIVERY_ORDER,"gyy_delivery_order_tag"),
    /**
     * 管易发货历史订单tag
     */
    GYY_DELIVERY_HISTORY_ORDER_TAG(4,RocketMqTagEnum.DELIVERY_ORDER,"gyy_delivery_history_order_tag"),
    /**
     * 管易退款订单tag
     */
    GYY_REFUND_ORDER_TAG(5,RocketMqTagEnum.REFUND_ORDER,"gyy_refund_order_tag"),
    /**
     * 管易退货订单tag
     */
    GYY_RETURN_ORDER_TAG(6,RocketMqTagEnum.RETURN_ORDER,"gyy_return_order_tag"),
    /**
     * 管易店铺信息tag
     */
    GYY_SHOP_INFO_TAG(7,RocketMqTagEnum.SHOP_INFO,"gyy_shop_info_tag"),
    /**
     * 管易SKU信息tag
     */
    GYY_SKU_INFO_TAG(8,RocketMqTagEnum.SKU_INFO,"gyy_sku_info_tag"),

    /**
     * 金蝶销售订单tag
     */
    KINGDEE_SALE_ORDER_TAG(9,RocketMqTagEnum.SALES_ORDER,"kingdee_sales_order_tag"),

    /**
     * 金蝶发货订单tag
     */
    KINGDEE_DELIVERY_ORDER_TAG(10,RocketMqTagEnum.DELIVERY_ORDER,"kingdee_delivery_order_tag"),
    /**
     * 金蝶退款订单tag
     */
    KINGDEE_REFUND_ORDER_TAG(11,RocketMqTagEnum.REFUND_ORDER,"kingdee_refund_order_tag"),
    /**
     * 金蝶退货订单tag
     */
    KINGDEE_RETURN_ORDER_TAG(12,RocketMqTagEnum.RETURN_ORDER,"kingdee_return_order_tag"),
    /**
     * 金蝶订单tag
     */
    KINGDEE_SHOP_INFO_TAG(13,RocketMqTagEnum.SHOP_INFO,"kingdee_shop_info_tag"),
    /**
     * 金蝶退货订单tag
     */
    KINGDEE_ECC_SHOP_INFO_TAG(21,RocketMqTagEnum.SHOP_INFO,"kingdee_ecc_shop_info_tag"),
    /**
     * 金蝶sku信息tag
     */
    KINGDEE_SKU_INFO_TAG(14,RocketMqTagEnum.SKU_INFO,"kingdee_sku_info_tag"),
    /**
     * 马帮销售订单tag
     */
    MABANG_SALE_ORDER_TAG(15,RocketMqTagEnum.SALES_ORDER,"mabang_sales_order_tag"),

    /**
     * 马帮发货订单tag
     */
    MABANG_DELIVERY_ORDER_TAG(16,RocketMqTagEnum.DELIVERY_ORDER,"mabang_delivery_order_tag"),
    /**
     * 马帮退款订单tag
     */
    MABANG_REFUND_ORDER_TAG(17,RocketMqTagEnum.REFUND_ORDER,"mabang_refund_order_tag"),
    /**
     * 马帮退货订单tag
     */
    MABANG_RETURN_ORDER_TAG(18,RocketMqTagEnum.RETURN_ORDER,"mabang_return_order_tag"),
    /**
     * 马帮店铺信息tag
     */
    MABANG_SHOP_INFO_TAG(19, RocketMqTagEnum.SHOP_INFO,"mabang_shop_info_tag"),
    /**
     * 马帮sku信息tag
     */
    MABANG_SKU_INFO_TAG(20, RocketMqTagEnum.SKU_INFO,"mabang_sku_info_tag"),


    //---------------------------------金蝶数据同步------------------------------------------------------------------------------------------
    /**
     * 产品信息同步金蝶
     */
    KINGDEE_PRODUCT_DETAIL_TAG(21, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_product_detail_tag"),

    /**
     * BOM管理同步金蝶
     */
    KINGDEE_BOM_INFO_TAG(22, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_bom_info_tag"),

    /**
     * 辅助资料同步金蝶（产品分类、）
     */
    KINGDEE_ASSISTANT_DATA_TAG(23, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_assistant_data_tag"),


    GC_STOCK_INBOUND_ORDER_TAG(24,RocketMqTagEnum.STOCK,"gc_stock_inbound_order_tag"),

    IML_STOCK_INBOUND_ORDER_TAG(25,RocketMqTagEnum.STOCK,"iml_stock_inbound_order_tag"),

    /**
     * 同步产品信息到中台dmp
     */
    SYNC_DMP_PRODUCT_INFO_TAG(26,RocketMqTagEnum.SYNC_DMP,"sync_dmp_product_info_tag"),
    /**
     * 同步产品sku到中台dmp
     */
    SYNC_DMP_PRODUCT_SKU_TAG(27,RocketMqTagEnum.SYNC_DMP,"sync_dmp_product_sku_tag"),
    /**
     * 同步产品sku变更新老品
     */
    SYNC_DMP_PRODUCT_LISTING_TAG(28,RocketMqTagEnum.SYNC_DMP,"sync_dmp_product_listing_tag"),

    /**
     * 系统用户同步金蝶
     */
    KINGDEE_SYS_USER_INFO_TAG(30, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_sys_user_info_tag"),

    /**
     * 采购订单同步金蝶
     */
    KINGDEE_PURCHASE_ORDER_TAG(31, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_order_tag"),

    /**
     * 采购申请单同步金蝶
     */
    KINGDEE_PURCHASE_APPLICATION_ORDER_TAG(32, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_application_order_tag"),

    //-----------------------------消息通知--------------------------------------------

    /**
     * 消息通知，可以不同的业务使用不同的tag
     */
    MSG_NOTICE_TAG(29,RocketMqTagEnum.MSG_NOTICE,"msg_notice_default_tag"),

    //-----------------------------dmp数据更新--------------------------------------------

    /**
     * 店铺变更
     */
    SHOP_INFO_CHANGE_CHARGE_TAG(33, RocketMqTagEnum.SYNC_KINGDEE,"shop_info_change_charge_tag"),
    ;
    
    
    public static final String SALES_ORDER = "sales";
    public static final String DELIVERY_ORDER = "delivery";
    public static final String REFUND_ORDER = "refund";
    public static final String RETURN_ORDER = "return";
    public static final String SHOP_INFO = "shop";

    public static final String SKU_INFO = "sku";

    public static final String SYNC_KINGDEE = "sync_kingdee";

    public static final String STOCK = "stock";

    public static final String SYNC_DMP = "sync_dmp";

    public static final String MSG_NOTICE = "msg_notice";

    private Integer code;

    private String type;

    private String name;

    public Integer getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    RocketMqTagEnum(Integer code, String type, String name) {
        this.code = code;
        this.type = type;
        this.name = name;
    }

    public static List<RocketMqTagEnum> listByType(String type) {
        List<RocketMqTagEnum> collect = Arrays.stream(values()).filter(value -> value.getType().equals(type))
                .collect(Collectors.toList());
        return collect;
    }

    public static RocketMqTagEnum getByCode(Integer code) {
        return Arrays.stream(values()).filter(value -> value.getCode().equals(code))
                .findFirst().orElseGet(null);
    }
    public static String getTagStrByType(String type){
        List<RocketMqTagEnum> tagList = listByType(type);
        if (CollectionUtil.isEmpty(tagList)){
            return "";
        }
        return tagList.stream().map(RocketMqTagEnum::getName).collect(Collectors.joining("||"));
    }

    public static void main(String[] args) {
        String tagStrByType = getTagStrByType(SKU_INFO);
        System.out.println("tagStrByType = " + tagStrByType);
    }


}
