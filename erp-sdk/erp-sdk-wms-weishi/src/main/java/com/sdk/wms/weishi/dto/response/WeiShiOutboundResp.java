package com.sdk.wms.weishi.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiOutboundResp {

    @JSONField(name = "customerNo")
    private String customerNo;
    @JSONField(name = "orderNo")
    private String orderNo;
    @JSONField(name = "erpNo")
    private String erpNo;
    @JSONField(name = "trackingNo")
    private String trackingNo;
    @JSONField(name = "status")
    private Integer status;
    @JSONField(name = "interceptStatus")
    private Integer interceptStatus;
    @JSONField(name = "shippedTime")
    private Object shippedTime;
    @JSONField(name = "createTime")
    private String createTime;
    @JSONField(name = "errorMsg")
    private String errorMsg;
}
