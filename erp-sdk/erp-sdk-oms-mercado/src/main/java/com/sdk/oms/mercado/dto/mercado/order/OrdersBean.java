package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("id")
    private long fid;
    @JsonProperty("items")
    private Object items;
    @JsonProperty("feedback")
    private FeedbackBean feedback;
    @JsonProperty("seller")
    private SellerBean seller;
    @JsonProperty("payments")
    private List<PaymentsBean> payments;
    @JsonProperty("mediations")
    private List<?> mediations;

}
