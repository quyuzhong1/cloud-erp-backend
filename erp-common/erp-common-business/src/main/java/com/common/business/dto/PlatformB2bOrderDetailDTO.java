package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *收款单消费DTO
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformB2bOrderDetailDTO extends UniqueDto {

    /**
     * 平台明细Id
     */
    private String platformDetailId;

    /**
     * 平台sku
     */
    private String platformSkuNo;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 单价
     */
    private Integer price;

    /**
     * 平台是否已删除
     */
    private Boolean platformIsDeleted;
    /**
     * 是否赠品
     */
    private Boolean isGift;

    /**
     * 是否作废
     */
    private Boolean isInvalid;

    private String skuId;

    private String skuNo;
}
