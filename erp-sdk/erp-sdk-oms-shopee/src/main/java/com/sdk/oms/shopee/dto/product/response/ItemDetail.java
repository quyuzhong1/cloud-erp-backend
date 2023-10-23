package com.sdk.oms.shopee.dto.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Image
 * @description: TODO
 * @date 2023年10月20日
 * @version: 1.0
 */
@Data
public class ItemDetail implements Serializable {
    @JsonProperty("item_id")
    private Long itemId;
    @JsonProperty("item_status")
    private String itemStatus;
    @JsonProperty("update_time")
    private Long updateTime;
}
