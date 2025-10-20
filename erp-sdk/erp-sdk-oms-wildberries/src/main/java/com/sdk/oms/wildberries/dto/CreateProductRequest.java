package com.sdk.oms.wildberries.dto;

import jnr.ffi.annotations.In;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
@Builder
public class CreateProductRequest {
    private Integer subjectID;
    private List<Variant> variants;
    @Data
    @Builder
    public static class Variant{
        private String vendorCode;
        private Wholesale wholesale;
        private String title;
        private String description;
        private String brand;
        private Dimension dimensions;
        private List<Characteristic> characteristics;
        private List<Size> sizes;
    }
    @Data
    @Builder
    public static class Wholesale{
        private Boolean enabled;
        private Integer quantum;
    }
    @Data
    @Builder
    public static class Dimension{
        private Integer length;
        private Integer width;
        private Integer height;
        private BigDecimal weightBrutto;
    }
    @Data
    @Builder
    public static class Characteristic{
        private Integer id;
        private Object value;
    }
    @Data
    @Builder
    public static class Size{
        private String techSize;
        private String wbSize;
        private BigDecimal price;
        private List<String> skus;
    }
}
