package com.sdk.oms.walmart.dto.walmart.order;

import com.google.gson.annotations.SerializedName;
import com.sdk.oms.walmart.dto.walmart.ship.ReturnCenterAddressBean;
import com.sdk.oms.walmart.dto.walmart.ship.TrackingInfoBean;

public class OrderLineStatusBean {
    /**
     * status : Created
     * subSellerId : null
     * statusQuantity : {"unitOfMeasurement":"EACH","amount":"1"}
     * cancellationReason : null
     * trackingInfo : null
     * returnCenterAddress : null
     */

    @SerializedName("status")
    private String status;
    @SerializedName("subSellerId")
    private Object subSellerId;
    @SerializedName("statusQuantity")
    private StatusQuantityBean statusQuantity;
    @SerializedName("cancellationReason")
    private String cancellationReason;
    @SerializedName("trackingInfo")
    private TrackingInfoBean trackingInfo;
    @SerializedName("returnCenterAddress")
    private ReturnCenterAddressBean returnCenterAddress;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getSubSellerId() {
        return subSellerId;
    }

    public void setSubSellerId(Object subSellerId) {
        this.subSellerId = subSellerId;
    }

    public StatusQuantityBean getStatusQuantity() {
        return statusQuantity;
    }

    public void setStatusQuantity(StatusQuantityBean statusQuantity) {
        this.statusQuantity = statusQuantity;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public TrackingInfoBean getTrackingInfo() {
        return trackingInfo;
    }

    public void setTrackingInfo(TrackingInfoBean trackingInfo) {
        this.trackingInfo = trackingInfo;
    }

    public ReturnCenterAddressBean getReturnCenterAddress() {
        return returnCenterAddress;
    }

    public void setReturnCenterAddress(ReturnCenterAddressBean returnCenterAddress) {
        this.returnCenterAddress = returnCenterAddress;
    }
}
