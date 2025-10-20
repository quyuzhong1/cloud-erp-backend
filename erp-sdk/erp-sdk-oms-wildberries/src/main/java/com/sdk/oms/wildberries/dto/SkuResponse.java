package com.sdk.oms.wildberries.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zdy
 * @ClassName SkuResponse
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SkuResponse extends BaseResponse{

    /**
     * 商品列表
     */
    private List<Card> cards;

    private Cursor cursor;
    @Data
    public static class Cursor{
        private Integer total;
        private String updatedAt;
        private Long nmID;
    }
    @Data
    public static class Card{
        private Long nmID;
        private Long imtID;
        private String nmUUID;
        private Long subjectID;
        private String subjectName;
        private String vendorCode;
        private String brand;
        private String title;
        private String description;
        private Boolean needKiz;
        private List<Photo> photos;
        private Dimensions dimensions;
        private List<Characteristic> characteristics;
        private List<Size> sizes;
        private String createdAt;
        private String updatedAt;
    }
    @Data
    public static class Photo{
        private String big;
        private String c246x328;
        private String c516x688;
        private String hq;
        private String square;
        private String tm;
    }
    @Data
    public static class Dimensions{
        private Integer width;
        private Integer height;
        private Integer length;
        private BigDecimal weightBrutto;
        private Boolean isValid;
    }

    @Data
    public static class Characteristic{
        private Long id;
        private String name;
        private List<String> value;
    }
    @Data
    public static class Size{
        private Long chrtID;
        private String techSize;
        private String wbSize;
        private List<String> skus;
    }

}
