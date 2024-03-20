package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AtmTransferReferenceBean {
    /**
     * company_id : null
     * transaction_id : 611672965
     */

    @JsonProperty("company_id")
    private Object companyId;
    @JsonProperty("transaction_id")
    private String transactionId;

    public Object getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Object companyId) {
        this.companyId = companyId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
