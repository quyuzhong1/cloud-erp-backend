package com.erp.tms.batong.constants;

/**
 * @author Lambda
 * @Classname BaTongConstant

 * @Date 2024-01-12 18:33
 * @Created by yl
 */
public class BaTongConstants {

    private BaTongConstants() {
        
    }

    /**
     * 成功
     */
    public static final Integer SUCCESS=1;

    /**
     *  失败
     */
    public static final Integer FAIL=0;

    /**
     * 基础url
     */
    public static final String BASE_URL="http://btgyl.rtb56.com/webservice/PublicService.asmx/ServiceInterfaceUTF8";

    /**
     * 创建订单
     */
    public static final String POST_CREATE_ORDER_URL="createorder";

    /**
     * 删除订单
     */
    public static final String DELETE_ORDER_URL="removeorder";

    /**
     * 获取标签
     */
    public static final String LABEL_URL="getnewlabel";

    /**
     * 获取标签
     */
    public static final String GET_TRACK_URL="gettrackingnumber";

    /**
     * 获取运输方式
     */
    public static final String GET_SHIPPING_METHOD="getshippingmethod";


    /**
     * 更新重量
     */
    public static final String UPDATE_ORDER="updateorder";
}
