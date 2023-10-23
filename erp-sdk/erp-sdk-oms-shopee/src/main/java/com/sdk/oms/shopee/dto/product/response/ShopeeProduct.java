package com.sdk.oms.shopee.dto.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zdy
 * @ClassName ShopeeProdcut
 * @description: TODO
 * @date 2023年10月20日
 * @version: 1.0
 */
@Data
@Builder
public class ShopeeProduct implements Serializable {
    @JsonProperty(value = "total_count")
    private Integer totalCount;
    @JsonProperty(value = "has_next_page")
    private Boolean hasNextPage;
    private String next;
    private List<Item> items = new LinkedList<>();
}
