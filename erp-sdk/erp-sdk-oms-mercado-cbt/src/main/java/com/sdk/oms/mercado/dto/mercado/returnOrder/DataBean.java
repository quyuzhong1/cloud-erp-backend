package com.sdk.oms.mercado.dto.mercado.returnOrder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataBean {
    /**
     * id : 5289013208
     * type : cancel_purchase
     * stage : none
     * status : closed
     * parent_id : null
     * client_id : null
     * resource_id : 43667056891
     * resource : shipment
     * reason_id : PNR9509
     * fulfilled : false
     * players : [{"role":"complainant","type":"receiver","user_id":700096957,"available_actions":[]},{"role":"respondent","type":"sender","user_id":1512693686,"available_actions":[]}]
     * resolution : null
     * labels : null
     * site_id : MLM
     * date_created : 2024-07-31T13:57:59.000-04:00
     * last_updated : 2024-07-31T13:58:02.000-04:00
     */

    @JsonProperty("id")
    private long fid;
    @JsonProperty("type")
    private String type;
    @JsonProperty("stage")
    private String stage;
    @JsonProperty("status")
    private String status;
    @JsonProperty("parent_id")
    private Object parentId;
    @JsonProperty("client_id")
    private Object clientId;
    @JsonProperty("resource_id")
    private long resourceId;
    @JsonProperty("resource")
    private String resource;
    @JsonProperty("reason_id")
    private String reasonId;
    @JsonProperty("fulfilled")
    private boolean fulfilled;
    @JsonProperty("resolution")
    private Object resolution;
    @JsonProperty("labels")
    private Object labels;
    @JsonProperty("site_id")
    private String siteId;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("last_updated")
    private String lastUpdated;
    @JsonProperty("players")
    private List<PlayersBean> players;

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getParentId() {
        return parentId;
    }

    public void setParentId(Object parentId) {
        this.parentId = parentId;
    }

    public Object getClientId() {
        return clientId;
    }

    public void setClientId(Object clientId) {
        this.clientId = clientId;
    }

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getReasonId() {
        return reasonId;
    }

    public void setReasonId(String reasonId) {
        this.reasonId = reasonId;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public Object getResolution() {
        return resolution;
    }

    public void setResolution(Object resolution) {
        this.resolution = resolution;
    }

    public Object getLabels() {
        return labels;
    }

    public void setLabels(Object labels) {
        this.labels = labels;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<PlayersBean> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayersBean> players) {
        this.players = players;
    }
}
