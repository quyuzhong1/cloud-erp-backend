package com.sdk.oms.mercadolocal.dto.mercadolocal.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextBean {
    /**
     * application : null
     * product_id : null
     * channel : marketplace
     * site : MLB
     * flows : ["b2b"]
     */

    @JsonProperty("application")
    private Object application;
    @JsonProperty("product_id")
    private Object productId;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("site")
    private String site;
    @JsonProperty("flows")
    private List<String> flows;

    public Object getApplication() {
        return application;
    }

    public void setApplication(Object application) {
        this.application = application;
    }

    public Object getProductId() {
        return productId;
    }

    public void setProductId(Object productId) {
        this.productId = productId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public List<String> getFlows() {
        return flows;
    }

    public void setFlows(List<String> flows) {
        this.flows = flows;
    }
}
