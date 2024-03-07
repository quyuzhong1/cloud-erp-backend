package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class EstimatedDeliveryTimeBean {
    /**
     * type : known_frame
     * date : 2024-03-01T00:00:00.000-06:00
     * unit : hour
     * offset : {"date":"2024-03-14T00:00:00.000-06:00","shipping":216}
     * time_frame : {"from":"","to":""}
     * pay_before : 2024-02-19T10:00:00.000-06:00
     * shipping : 168
     * handling : 72
     * schedule : null
     */

    @SerializedName("type")
    private String type;
    @SerializedName("date")
    private String date;
    @SerializedName("unit")
    private String unit;
    @SerializedName("offset")
    private OffsetBean offset;
    @SerializedName("time_frame")
    private TimeFrameBean timeFrame;
    @SerializedName("pay_before")
    private String payBefore;
    @SerializedName("shipping")
    private int shipping;
    @SerializedName("handling")
    private int handling;
    @SerializedName("schedule")
    private Object schedule;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public OffsetBean getOffset() {
        return offset;
    }

    public void setOffset(OffsetBean offset) {
        this.offset = offset;
    }

    public TimeFrameBean getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(TimeFrameBean timeFrame) {
        this.timeFrame = timeFrame;
    }

    public String getPayBefore() {
        return payBefore;
    }

    public void setPayBefore(String payBefore) {
        this.payBefore = payBefore;
    }

    public int getShipping() {
        return shipping;
    }

    public void setShipping(int shipping) {
        this.shipping = shipping;
    }

    public int getHandling() {
        return handling;
    }

    public void setHandling(int handling) {
        this.handling = handling;
    }

    public Object getSchedule() {
        return schedule;
    }

    public void setSchedule(Object schedule) {
        this.schedule = schedule;
    }
}
