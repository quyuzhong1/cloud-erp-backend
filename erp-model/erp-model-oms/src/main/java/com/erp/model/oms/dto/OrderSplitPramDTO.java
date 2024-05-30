package com.erp.model.oms.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class OrderSplitPramDTO {

    @SerializedName("splittable_groups")
    private List<SplittableGroupsBean> splittableGroups;

}
