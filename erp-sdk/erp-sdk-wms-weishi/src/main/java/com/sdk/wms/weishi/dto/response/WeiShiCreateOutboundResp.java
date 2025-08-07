package com.sdk.wms.weishi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiCreateOutboundResp {

    @JsonProperty("orderNo")
    private String orderNo;
    @JsonProperty("referNo")
    private String referNo;
    @JsonProperty("extendTrackingNo")
    private String extendTrackingNo;
    @JsonProperty("trackingNumber")
    private String trackingNumber;
    @JsonProperty("carrier")
    private String carrier;
}
