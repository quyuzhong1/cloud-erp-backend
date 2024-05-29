package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsBean {
    /**
     * id : 2000007633674134
     * buyer : {"id":139133205}
     * config : {"items":[{"id":"MLM2802963514"}]}
     * orders : [{"id":2000007633674134,"items":null,"feedback":{"purchase":null,"sale":null},"payments":[{"id":72480387497}],"mediations":[],"seller":{"id":1511265855}}]
     * shipment : {"id":43116658829,"payments":[]}
     */

    @JsonProperty("id")
    private long fid;
    @JsonProperty("buyer")
    private BuyerBean buyer;
    @JsonProperty("config")
    private ConfigBean config;
    @JsonProperty("shipment")
    private ShipmentBean shipment;
    @JsonProperty("orders")
    private List<OrdersBean> orders;

}
