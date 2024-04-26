package com.sdk.tms.track123.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 海运返回参数
 * @author Will
 * @date: 2024/4/9 15:42
 */
@Data
public class OceanTrackInfo implements Serializable {

    /**
     * 单号
     */
    private String trackingNo;

    /**
     * 单号类型（1.订舱号 2.提单号 3.箱号）
     */
    private String type;

    /**
     * 查询状态
     */
    private String searchStatus;

    /**
     * 唯一订单号(由track123生成的订单编号，从注册接口的响应参数获取)
     */
    private String orderNo;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 用户邮箱
     */
    private String customerEmail;

    /**
     *  承运人信息
     */
    private OceanContainerInfo carrierInfo;
}
