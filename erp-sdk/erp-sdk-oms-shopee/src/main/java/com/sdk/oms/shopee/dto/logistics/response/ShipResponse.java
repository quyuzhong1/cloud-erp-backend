package com.sdk.oms.shopee.dto.logistics.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShipResponse
 * @description: TODO
 * @date 2023年12月11日
 * @version: 1.0
 */
@Data
public class ShipResponse implements Serializable {
    @Alias( "request_id")
    private String requestId;
    @Alias( "error")
    private String error;
    @Alias( "message")
    private String message;
    @Alias( "response")
    private ShipDetailResponse response;
}
