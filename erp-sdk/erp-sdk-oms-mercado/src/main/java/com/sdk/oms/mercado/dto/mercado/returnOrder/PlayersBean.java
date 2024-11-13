package com.sdk.oms.mercado.dto.mercado.returnOrder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayersBean {
    /**
     * role : complainant
     * type : receiver
     * user_id : 700096957
     * available_actions : []
     */

    @JsonProperty("role")
    private String role;
    @JsonProperty("type")
    private String type;
    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("available_actions")
    private List<?> availableActions;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<?> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<?> availableActions) {
        this.availableActions = availableActions;
    }
}
