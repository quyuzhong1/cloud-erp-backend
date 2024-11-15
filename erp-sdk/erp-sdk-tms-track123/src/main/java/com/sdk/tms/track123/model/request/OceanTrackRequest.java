package com.sdk.tms.track123.model.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName TrackRequest

 * @date 2023年11月07日
 * @version: 1.0
 */
@Data
@Builder
public class OceanTrackRequest implements Serializable {

    /**
     * 唯一订单号(由track123生成的订单编号，从注册接口的响应参数获取)
     */
    private String orderNo;

    /**
     * 查询单号
     */
    private String trackingNo;

    /**
     * 单号类型（1.订舱号 2.提单号 3.箱号）
     */
    private String type;
}
