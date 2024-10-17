package com.common.business.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台退货订单DTO
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformReturnOrderDTO extends UniqueDto {

    /**
     * 平台退货单号
     */
    private String platformReturnNo;
    /**
     * 平台订单编号
     */
    private String platformOrderNo;
    /**
     * 退货原因
     */
    private String reason;
    /**
     * 平台
     */
    private String dictPlatform;
    /**
     * 明细
     */
    private List<PlatformRefundOrderDTO.Detail> detailList;

    @Data
    @ToString
    public static class Detail {

        //商品SKU(第三方)
        private String platformSkuNo;

        //退货数量
        private Integer returnQty;

        //备注
        private String remark;

    }
}
