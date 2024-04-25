package com.sdk.oms.tiktok.dto.tiktok.ship;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataBean {
    /**
     * order_id : 32131324123321
     * order_line_item_ids : ["31322412312312"]
     * package_id : 32141235124234
     * warning : {"message":"match more than one provider"}
     */

    @SerializedName("order_id")
    private String orderId;
    @SerializedName("package_id")
    private String packageId;
    @SerializedName("warning")
    private WarningBean warning;
    @SerializedName("order_line_item_ids")
    private List<String> orderLineItemIds;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public WarningBean getWarning() {
        return warning;
    }

    public void setWarning(WarningBean warning) {
        this.warning = warning;
    }

    public List<String> getOrderLineItemIds() {
        return orderLineItemIds;
    }

    public void setOrderLineItemIds(List<String> orderLineItemIds) {
        this.orderLineItemIds = orderLineItemIds;
    }
}
