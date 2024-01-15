package com.erp.tms.batong.model.order.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname OrderResponse
 * @Description 创建物流单返回接口
 * @Date 2024-01-12 18:15
 * @Created by yl
 */
@Data
public class OrderResponse implements Serializable {

    /**
     * 订单id
     */
    @JSONField(name = "order_id")
    private String  orderId;


    /**
     * 客户参考号
     */
    @JSONField(name = "refrence_no")
    private String  refrenceNo;


    /**
     * 服务商单号
     */
    @JSONField(name = "shipping_method_no")
    private String  shippingMethodNo;


    /**
     * 渠道转单号
     */
    @JSONField(name = "channel_hawbcode")
    private String  channelHawbcode;
}
