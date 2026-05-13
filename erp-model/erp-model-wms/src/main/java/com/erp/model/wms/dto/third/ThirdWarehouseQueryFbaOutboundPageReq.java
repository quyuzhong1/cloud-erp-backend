package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 按时间分页查询B2B出库单，当前用于支持众包DMP init拉取。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseQueryFbaOutboundPageReq extends ThirdWarehouseAuth {

    private String startCreateTime;

    private String endCreateTime;

    private String startUpdateTime;

    private String endUpdateTime;

    private Integer pageNum;

    private Integer pageSize;
}
