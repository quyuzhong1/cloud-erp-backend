package com.sdk.wangdian.sdk.api.wms.external.out;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateStockExternalOutResponse {

    private String message;

    private Integer status;

    @SerializedName("data")
    private Map<String, Object> data;
}
