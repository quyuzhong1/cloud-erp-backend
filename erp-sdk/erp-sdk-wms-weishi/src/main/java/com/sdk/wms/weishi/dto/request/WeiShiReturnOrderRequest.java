package com.sdk.wms.weishi.dto.request;

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
public class WeiShiReturnOrderRequest {

    @JSONField(name = "beginTime")
    private String beginTime;
    @JSONField(name = "endTime")
    private String endTime;
}
