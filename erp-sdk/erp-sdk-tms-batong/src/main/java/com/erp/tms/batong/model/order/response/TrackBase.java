package com.erp.tms.batong.model.order.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname TrackBase
 * @Description 跟踪单号
 * @Date 2024-01-15 11:50
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackBase implements Serializable {

    /**
     * 客户参考号
     */
    @JSONField(name = "refrence_no")
    private String refrenceNo;



    /**
     * 服务商单号 就是跟踪单号
     */
    @JSONField(name = "shipping_method_no")
    private String shippingMethodNo;


    /**
     * 渠道转单号
     */
    @JSONField(name = "channel_hawbcode")
    private String channelHawbcode;

}
