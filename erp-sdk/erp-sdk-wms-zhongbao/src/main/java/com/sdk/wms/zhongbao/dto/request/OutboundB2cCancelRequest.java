package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class OutboundB2cCancelRequest implements Serializable {
    //订单号列表
    private List<String> orderNos;
    //取消原因
    private String cancelRemark;
}
