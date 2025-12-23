package com.sdk.wangdian.sdk.api;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorList {
    @SerializedName("error")
    private String error;
    @SerializedName("no")
    private String no;
}
