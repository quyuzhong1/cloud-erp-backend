package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContextBean {
    /**
     * channel : marketplace
     * site : MLM
     * flows : ["cbt"]
     * application : buyingflow-api
     */

    @SerializedName("channel")
    private String channel;
    @SerializedName("site")
    private String site;
    @SerializedName("application")
    private String application;
    @SerializedName("flows")
    private List<String> flows;

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

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<String> getFlows() {
        return flows;
    }

    public void setFlows(List<String> flows) {
        this.flows = flows;
    }
}
