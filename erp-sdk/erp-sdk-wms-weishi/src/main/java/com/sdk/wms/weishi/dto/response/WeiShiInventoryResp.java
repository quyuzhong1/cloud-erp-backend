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
public class WeiShiInventoryResp {

    @JSONField(name = "page")
    private PageDTO page;

    @NoArgsConstructor
    @Data
    public static class PageDTO {
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
        private String heads;


    }
    @NoArgsConstructor
    @Data
    public static class RowsDTO {
        private String warehouseCode;
        private String warehouseName;
        @JSONField(name = "sku")
        private String sku;
        @JSONField(name = "skuNo")
        private String skuNo;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "totalNum")
        private Integer totalNum;
        @JSONField(name = "availableNum")
        private Integer availableNum;
        @JSONField(name = "lockedNum")
        private Integer lockedNum;
        @JSONField(name = "onTheWayNum")
        private Integer onTheWayNum;
        @JSONField(name = "pendingListingNum")
        private Integer pendingListingNum;
        @JSONField(name = "nonGoodNum")
        private Integer nonGoodNum;
        @JSONField(name = "isDistribution")
        private Integer isDistribution;
        @JSONField(name = "errorMsg")
        private String errorMsg;
    }
}
