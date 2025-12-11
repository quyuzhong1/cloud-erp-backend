package com.sdk.wangdian.sdk.api;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Result {
    @SerializedName("error_list")
    private List<ErrorList> errorList;
}
