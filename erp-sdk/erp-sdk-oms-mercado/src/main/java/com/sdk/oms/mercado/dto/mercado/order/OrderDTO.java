package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDTO {

    /**
     * query :
     * results : [{"id":2000007633674134,"buyer":{"id":139133205},"config":{"items":[{"id":"MLM2802963514"}]},"orders":[{"id":2000007633674134,"items":null,"feedback":{"purchase":null,"sale":null},"payments":[{"id":72480387497}],"mediations":[],"seller":{"id":1511265855}}],"shipment":{"id":43116658829,"payments":[]}}]
     * sort : {"id":"date_asc","name":"Date ascending"}
     * available_sorts : [{"id":"date_desc","name":"Date descending"},{"id":"updated_asc","name":"Last Updated ascending"},{"id":"updated_desc","name":"Last Updated descending"},{"id":"closed_asc","name":"Date closed ascending"},{"id":"closed_desc","name":"Date closed descending"}]
     * filters : [{"id":"seller.id","name":"seller ID ","type":"text","values":["1511265855"]}]
     * paging : {"total":2,"limit":1,"offset":1}
     */

    @JsonProperty("query")
    private String query;
    @JsonProperty("sort")
    private SortBean sort;
    @JsonProperty("paging")
    private PagingBean paging;
    @JsonProperty("results")
    private List<ResultsBean> results;
    @JsonProperty("available_sorts")
    private List<AvailableSortsBean> availableSorts;
    @JsonProperty("filters")
    private List<FiltersBean> filters;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SortBean getSort() {
        return sort;
    }

    public void setSort(SortBean sort) {
        this.sort = sort;
    }

    public PagingBean getPaging() {
        return paging;
    }

    public void setPaging(PagingBean paging) {
        this.paging = paging;
    }

    public List<ResultsBean> getResults() {
        return results;
    }

    public void setResults(List<ResultsBean> results) {
        this.results = results;
    }

    public List<AvailableSortsBean> getAvailableSorts() {
        return availableSorts;
    }

    public void setAvailableSorts(List<AvailableSortsBean> availableSorts) {
        this.availableSorts = availableSorts;
    }

    public List<FiltersBean> getFilters() {
        return filters;
    }

    public void setFilters(List<FiltersBean> filters) {
        this.filters = filters;
    }
}
