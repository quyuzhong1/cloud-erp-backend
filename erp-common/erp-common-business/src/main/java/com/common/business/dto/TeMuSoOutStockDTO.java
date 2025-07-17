package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeMuSoOutStockDTO extends UniqueDto {

    /**
     * 平台
     */
    private String platformOrderCode;

    /**
     * 出库时间
     */
    private LocalDateTime outTime;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 运单号
     */
    private String transportNo;

    private List<TeMuSoOutStockDetailDTO> detailList;

}
