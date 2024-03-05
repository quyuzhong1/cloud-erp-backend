package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultsBean {
    /**
     * id : 2000007633674134
     * buyer : {"id":139133205}
     * config : {"items":[{"id":"MLM2802963514"}]}
     * orders : [{"id":2000007633674134,"items":null,"feedback":{"purchase":null,"sale":null},"payments":[{"id":72480387497}],"mediations":[],"seller":{"id":1511265855}}]
     * shipment : {"id":43116658829,"payments":[]}
     */

    @SerializedName("id")
    private long id;
    @SerializedName("buyer")
    private BuyerBean buyer;
    @SerializedName("config")
    private ConfigBean config;
    @SerializedName("shipment")
    private ShipmentBean shipment;
    @SerializedName("orders")
    private List<OrdersBean> orders;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BuyerBean getBuyer() {
        return buyer;
    }

    public void setBuyer(BuyerBean buyer) {
        this.buyer = buyer;
    }

    public ConfigBean getConfig() {
        return config;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }

    public ShipmentBean getShipment() {
        return shipment;
    }

    public void setShipment(ShipmentBean shipment) {
        this.shipment = shipment;
    }

    public List<OrdersBean> getOrders() {
        return orders;
    }

    public void setOrders(List<OrdersBean> orders) {
        this.orders = orders;
    }
}
