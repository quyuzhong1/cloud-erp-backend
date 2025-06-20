package com.sdk.wms.jifeng.dto.request;

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
public class JiFengReturnOrderRequest {

    @JSONField(name = "beginTime")
    private String beginTime;
    @JSONField(name = "endTime")
    private String endTime;
}
