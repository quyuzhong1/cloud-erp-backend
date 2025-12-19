package com.sdk.wms.damai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DaMaiGetFbaOrderRequest {
    /**
     * 订单号列表
     */
    @JsonProperty("fbaSoNoList")
    private List<String> fbaSoNoList;

    /**
     * 客户单号列表
     */
    @JsonProperty("custRefNoList")
    private List<String> custRefNoList;
}
