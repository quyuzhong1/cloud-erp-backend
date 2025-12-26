package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 平台退货订单DTO
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformB2BReturnOrderDTO extends UniqueDto {

    /**
     * 第三方编号
     */
    private String thirdCode;

    /**
     * 平台订单号
     */
    private String platformOrderCode;

    private String shopId;


    private String platformWarehouseId;

    /**
     * 退货物流单号
     */
    private String returnLogisticCode;
    /**
     * 退货日期
     */
    private LocalDate billDate;

    private String platformOrderType;
    /**
     * 明细
     */
    private List<PlatformB2BReturnOrderDTO.Detail> detailList;

    @Data
    @ToString
    public static class Detail {

        private String skuNo;

        private String platformSkuNo;

        private String skuId;

        private Integer returnQty;

        private BigDecimal returnAmount;

        private String returnTypeDict;

        private String returnReasonDict;

        private String remark;
    }
}
