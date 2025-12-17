package com.sdk.wms.weishi.dto.request;

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
public class WeiShiQueryInboundRequest {

    private List<String> orderNoList;

    private Integer pageNum;

    private Integer pageSize;
}
