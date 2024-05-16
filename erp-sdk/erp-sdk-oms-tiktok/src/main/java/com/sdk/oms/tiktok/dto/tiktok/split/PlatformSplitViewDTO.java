package com.sdk.oms.tiktok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class PlatformSplitViewDTO {

    /**
     * code : 0
     * data : {"packages":[{"id":"1153405976774411274","splittable_group_id":"1153405976774411385"},{"id":"1153405976774280202","splittable_group_id":"12333"},{"id":"1153405976774345738","splittable_group_id":"1153405976774345849"}]}
     * message : Success
     * request_id : 20240412081827D6E40ACBD1BC33004E4F
     */

    @SerializedName("code")
    private int code;
    @SerializedName("data")
    private SplitViewBean data;
    @SerializedName("message")
    private String message;
    @SerializedName("request_id")
    private String requestId;

}
