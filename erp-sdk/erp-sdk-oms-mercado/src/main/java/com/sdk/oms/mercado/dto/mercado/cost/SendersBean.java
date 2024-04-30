package com.sdk.oms.mercado.dto.mercado.cost;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendersBean {
    /**
     * user_id : 1511265855
     * cost : 5.46
     * compensation : 0
     * save : 0
     * discounts : []
     * compensations : [{"amount":1.45,"reason":"incorrect_dimensions","comment":"Incorrect package dimensions"}]
     */

    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("cost")
    private double cost;
    @JsonProperty("compensation")
    private int compensation;
    @JsonProperty("save")
    private int save;
    @JsonProperty("discounts")
    private List<?> discounts;
    @JsonProperty("compensations")
    private List<CompensationsBean> compensations;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getCompensation() {
        return compensation;
    }

    public void setCompensation(int compensation) {
        this.compensation = compensation;
    }

    public int getSave() {
        return save;
    }

    public void setSave(int save) {
        this.save = save;
    }

    public List<?> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<?> discounts) {
        this.discounts = discounts;
    }

    public List<CompensationsBean> getCompensations() {
        return compensations;
    }

    public void setCompensations(List<CompensationsBean> compensations) {
        this.compensations = compensations;
    }
}
