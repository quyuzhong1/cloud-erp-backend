package com.sdk.tms.baohong.dto.request;

import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.HeaderRequest;
import lombok.Data;

/**
 * @author liuruipeng
 * @date 2024年01月23日 10:46
 */
@Data
public class BaoHongCreateOrderReq {
    private HeaderRequest headerRequest;
    private CreateOrderInfo orderInfo;

}
