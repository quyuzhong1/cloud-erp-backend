package com.sdk.wms.tongyou.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通邮查询派送订单响应。
 *
 * @author will
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TongYouQueryOutboundBillResp extends TongYouBaseResp<List<TongYouQueryOutboundResp>> {

    @JSONField(name = "succ_array")
    private Object succArray;

    @JSONField(name = "error_array")
    private Object errorArray;
}
