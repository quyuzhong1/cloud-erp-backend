package com.sdk.oms.tiktok.dto.tiktok.packages;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class CombinePackagePramDTO {

    @SerializedName("combinable_packages")
    private List<CombinePackageGroupsBean> combinablePackages;

}
