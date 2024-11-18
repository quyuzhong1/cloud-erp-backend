package com.sdk.tms.disifang.model.order.request;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DsfUpdateWeightReq implements Serializable {
    /**
     * 请求单号(支持4PX单号/客户单号/服务商单号)
     */
    @Alias("request_no")
    private String requestNo;
    /**
     * 预报重量（g）
     */
    @Alias("weight")
    private String weight;
}
