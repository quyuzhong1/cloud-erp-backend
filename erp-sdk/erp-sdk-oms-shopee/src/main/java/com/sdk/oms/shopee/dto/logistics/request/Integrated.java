package com.sdk.oms.shopee.dto.logistics.request;

import cn.hutool.core.annotation.Alias;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Integrated
 * @description: TODO
 * @date 2024年10月10日
 * @version: 1.0
 */
@Data
@Builder
public class Integrated implements Serializable {
    @Alias( "tracking_number")
    private String trackingNumber;
}
