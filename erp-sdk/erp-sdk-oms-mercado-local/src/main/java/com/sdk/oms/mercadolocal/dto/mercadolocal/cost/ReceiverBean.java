package com.sdk.oms.mercadolocal.dto.mercadolocal.cost;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiverBean {
    /**
     * user_id : 139133205
     * cost : 0
     * compensation : 0
     * save : 0
     * discounts : [{"rate":1,"type":"ratio","promoted_amount":5.46}]
     * compensations : []
     */

    @JsonProperty("user_id")
    private long userId;
    @JsonProperty("cost")
    private double cost;
    @JsonProperty("compensation")
    private int compensation;
    @JsonProperty("save")
    private int save;
    @JsonProperty("discounts")
    private List<DiscountsBean> discounts;
    @JsonProperty("compensations")
    private List<?> compensations;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(int cost) {
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

    public List<DiscountsBean> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<DiscountsBean> discounts) {
        this.discounts = discounts;
    }

    public List<?> getCompensations() {
        return compensations;
    }

    public void setCompensations(List<?> compensations) {
        this.compensations = compensations;
    }
}
