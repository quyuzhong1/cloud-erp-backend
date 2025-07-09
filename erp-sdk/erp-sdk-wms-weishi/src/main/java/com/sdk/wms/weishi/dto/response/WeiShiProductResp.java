package com.sdk.wms.weishi.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
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
public class WeiShiProductResp {

    @JSONField(name = "pageNo")
    private Integer pageNo;
    @JSONField(name = "pageSize")
    private Integer pageSize;
    @JSONField(name = "totalPage")
    private Integer totalPage;
    @JSONField(name = "totalSize")
    private Integer totalSize;
    @JSONField(name = "rows")
    private List<RowsDTO> rows;
    @JSONField(name = "heads")
    private Object heads;

    @NoArgsConstructor
    @Data
    public static class RowsDTO {
        @JSONField(name = "skuNo")
        private String skuNo;
        @JSONField(name = "sku")
        private String sku;
        @JSONField(name = "skuCode")
        private String skuCode;
        @JSONField(name = "skuCodeList")
        private List<String> skuCodeList;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "imgUrl")
        private String imgUrl;
        @JSONField(name = "length")
        private Double length;
        @JSONField(name = "width")
        private Double width;
        @JSONField(name = "height")
        private Double height;
        @JSONField(name = "weight")
        private Double weight;
        @JSONField(name = "actualLength")
        private Double actualLength;
        @JSONField(name = "actualWidth")
        private Double actualWidth;
        @JSONField(name = "actualHeight")
        private Double actualHeight;
        @JSONField(name = "actualWeight")
        private Double actualWeight;
        @JSONField(name = "declareChineseName")
        private String declareChineseName;
        @JSONField(name = "declareEnglishName")
        private String declareEnglishName;
        @JSONField(name = "declarePrice")
        private Object declarePrice;
        @JSONField(name = "declareCurrency")
        private Object declareCurrency;
        @JSONField(name = "declareAttribute")
        private Object declareAttribute;
        @JSONField(name = "createTime")
        private String createTime;
        @JSONField(name = "updateTime")
        private String updateTime;
    }
}
