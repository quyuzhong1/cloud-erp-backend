package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataListBean {
    @SerializedName("erp_fulfillment_forward_dto")
    private List<ErpFulfillmentForwardDtoBean> erpFulfillmentForwardDto;

    public List<ErpFulfillmentForwardDtoBean> getErpFulfillmentForwardDto() {
        return erpFulfillmentForwardDto;
    }

    public void setErpFulfillmentForwardDto(List<ErpFulfillmentForwardDtoBean> erpFulfillmentForwardDto) {
        this.erpFulfillmentForwardDto = erpFulfillmentForwardDto;
    }
}
