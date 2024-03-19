package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
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
    private long fid;
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

}
