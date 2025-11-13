package com.sdk.oms.wildberries.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SkuRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
@Builder
public class SkuRequest implements Serializable {
    private Setting settings;

    @Data
    @Builder
    public static class Setting{
        private Sort sort;
        private Filter filter;
        private Cursor cursor;
    }

    @Data
    @Builder
    public static class Sort{
        private Boolean ascending;
    }
    @Data
    @Builder
    public static class Filter{
        private String textSearch;
        private Boolean allowedCategoriesOnly;
        private Integer withPhoto;
    }

    @Data
    @Builder
    public static class Cursor{
        private Integer limit;
    }
}
