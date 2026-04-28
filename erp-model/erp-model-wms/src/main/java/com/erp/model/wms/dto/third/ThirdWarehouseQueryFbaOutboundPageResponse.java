package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseQueryFbaOutboundPageResponse {

    private String pageNum;

    private String pageSize;

    private String totalCount;

    private String totalPage;

    private List<ThirdWarehouseQueryFbaOutboundResponse> list;
}
