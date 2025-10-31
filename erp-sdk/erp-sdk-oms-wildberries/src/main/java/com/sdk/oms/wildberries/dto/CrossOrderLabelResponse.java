package com.sdk.oms.wildberries.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CrossOrderLabelResponse extends BaseResponse{
    private List<Sticker> stickers;


    /**
     * "orderID": 5346346,
     * "url": "http://.../some-sticker",
     * "parcelID": "WB0000000001"
     */
    @Data
    public static class Sticker{
        private Long orderId;
        private String url;
        private Long parcelId;
//        private String barcode;
//        private String file;
    }
}
