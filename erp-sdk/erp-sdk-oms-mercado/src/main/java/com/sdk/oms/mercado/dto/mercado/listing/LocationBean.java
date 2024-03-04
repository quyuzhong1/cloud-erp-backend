package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationBean {
    @SerializedName("location")
    public String location;
}
