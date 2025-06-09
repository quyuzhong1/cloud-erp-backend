package com.sdk.tms.shopee.model.logistics.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ShipInfo

 * @date 2024年10月09日
 * @version: 1.0
 */
@Data
public class ShipInfo implements Serializable {
    @Alias( "dropoff")
    private List<String> dropoffList;
    @Alias( "pickup")
    private List<String> pickupList;
    @Alias( "non_integrated")
    private List<String> nonIntegratedList;
}
