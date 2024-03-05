package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrdersBean {
    /**
     * id : 2000007633674134
     * items : null
     * feedback : {"purchase":null,"sale":null}
     * payments : [{"id":72480387497}]
     * mediations : []
     * seller : {"id":1511265855}
     */

    @SerializedName("id")
    private long id;
    @SerializedName("items")
    private Object items;
    @SerializedName("feedback")
    private FeedbackBean feedback;
    @SerializedName("seller")
    private SellerBean seller;
    @SerializedName("payments")
    private List<PaymentsBean> payments;
    @SerializedName("mediations")
    private List<?> mediations;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Object getItems() {
        return items;
    }

    public void setItems(Object items) {
        this.items = items;
    }

    public FeedbackBean getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackBean feedback) {
        this.feedback = feedback;
    }

    public SellerBean getSeller() {
        return seller;
    }

    public void setSeller(SellerBean seller) {
        this.seller = seller;
    }

    public List<PaymentsBean> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentsBean> payments) {
        this.payments = payments;
    }

    public List<?> getMediations() {
        return mediations;
    }

    public void setMediations(List<?> mediations) {
        this.mediations = mediations;
    }
}
