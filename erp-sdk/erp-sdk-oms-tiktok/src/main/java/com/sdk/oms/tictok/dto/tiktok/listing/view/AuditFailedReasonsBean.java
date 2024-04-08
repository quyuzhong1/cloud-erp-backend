package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AuditFailedReasonsBean {
    /**
     * position : product
     * reasons : ["violate listing rules"]
     * suggestions : ["The product violates TikTok Shopping listing rules, please check and resubmit."]
     */

    @JsonProperty("position")
    private String position;
    @JsonProperty("reasons")
    private List<String> reasons;
    @JsonProperty("suggestions")
    private List<String> suggestions;


}
