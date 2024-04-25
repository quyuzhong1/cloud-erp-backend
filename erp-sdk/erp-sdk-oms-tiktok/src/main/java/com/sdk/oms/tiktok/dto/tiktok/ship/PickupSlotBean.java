package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class PickupSlotBean {
    /**
     * end_time : 1623812664
     * start_time : 1623812664
     */

    @SerializedName("end_time")
    private int endTime;
    @SerializedName("start_time")
    private int startTime;

}
