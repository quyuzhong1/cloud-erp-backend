package com.sdk.tms.shopee.model.logistics.response;

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
public class ShipDetailResponse implements Serializable {
    @Alias( "info_needed")
    private ShipInfo infoNeeded;
    @Alias( "dropoff")
    private ShipDropInfo dropoff;
    @Alias( "pickup")
    private ShipPickInfo pickup;
}
