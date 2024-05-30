package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;

public class ShipOrderOtherParam {


    /**
     * handover_method : PICKUP
     * pickup_slot : {"end_time":1623812664,"start_time":1623812664}
     * self_shipment : {"shipping_provider_id":"6617675021119438849","tracking_number":"JX12345"}
     */

    @SerializedName("handover_method")
    private String handoverMethod;
    @SerializedName("pickup_slot")
    private PickupSlotBean pickupSlot;
    @SerializedName("self_shipment")
    private SelfShipmentBean selfShipment;

    public String getHandoverMethod() {
        return handoverMethod;
    }

    public void setHandoverMethod(String handoverMethod) {
        this.handoverMethod = handoverMethod;
    }

    public PickupSlotBean getPickupSlot() {
        return pickupSlot;
    }

    public void setPickupSlot(PickupSlotBean pickupSlot) {
        this.pickupSlot = pickupSlot;
    }

    public SelfShipmentBean getSelfShipment() {
        return selfShipment;
    }

    public void setSelfShipment(SelfShipmentBean selfShipment) {
        this.selfShipment = selfShipment;
    }
}
