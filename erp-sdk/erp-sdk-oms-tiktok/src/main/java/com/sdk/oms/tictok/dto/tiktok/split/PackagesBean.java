package com.sdk.oms.tictok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class PackagesBean {
    /**
     * id : 1153405976774411274
     * splittable_group_id : 1153405976774411385
     */

    @SerializedName("id")
    private String id;
    @SerializedName("splittable_group_id")
    private String splittableGroupId;

}
