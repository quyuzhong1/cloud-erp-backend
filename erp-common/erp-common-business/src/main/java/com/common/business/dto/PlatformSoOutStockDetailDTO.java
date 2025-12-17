package com.common.business.dto;

import com.common.core.utils.date.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售出库详情DTO
 *
 * @Author Jim
 * {@code @Date} 2024/03/06
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformSoOutStockDetailDTO implements Serializable {

    /**
     * 平台配送明细唯一ID
     * 亚马逊=shipmentItemId
     */
    private String platformDetailId;

    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * 平台订单明细id
     */
    private String platformOrderDetailId;

    /**
     * 平台下单时间
     */
    private String platformOrderCreateTime;

    /**
     * 平台付款时间
     */
    private String platformPayTime;

    /**
     * 平台配送时间
     */
    private String platformDeliveryTime;

    /**
     * 配送数量
     */
    private Integer qtyShipped;

    /**
     * 跟踪号
     */
    private String trackNo;

    /**
     * 仓库id
     */
    private String warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 库存组织
     */
    private String warehouseOrgId;

    /**
     * 库存组织名称
     */
    private String warehouseOrgName;

    /**
     * 仓储中心(亚马逊专用)
     */
    private String fulfillmentCenterId;
    /**
     * 卖家订单id
     */
    private String merchantOrderId;
    /**
     * 卖家订单明细id
     */
    private String merchantOrderItemId;

    public LocalDate convertPlatformDeliveryDateTime() {
        LocalDateTime localDateTime = DateUtil.parseLocalDateTimeWithOffset(this.getPlatformDeliveryTime());
        return null == localDateTime ? null : localDateTime.toLocalDate();
    }
}
