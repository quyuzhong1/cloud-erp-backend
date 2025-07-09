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
public class WeiShiChannelResp {

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
        private String totalSize;
        @JSONField(name = "rows")
        private List<RowsDTO> rows;

    }
    @NoArgsConstructor
    @Data
    public static class RowsDTO {
        @JSONField(name = "id")
        private Integer id;
        @JSONField(name = "code")
        private String code;
        @JSONField(name = "name")
        private String name;
    }
}
