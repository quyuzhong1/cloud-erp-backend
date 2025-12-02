package com.sdk.oms.mercadolocal.dto.mercadolocal.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentSlaViewDTO {

    /**
     * status: "on_time",
     * expected_date: "2024-05-22T23:59:59-03:00",
     * last_updated: "2024-05-21T17:16:04Z"
     */
    private Long fid;
    @JsonProperty("status")
    private String status;
    @JsonProperty("expected_date")
    private String expectedDate;
    @JsonProperty("last_updated")
    private String lastUpdated;



}
