package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 销售出库DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 * {@code @Date} 2024/03/06
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformSoOutStockDTO extends UniqueDto {

    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * 销售平台
     */
    private String dictPlatform;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 收货国家
     */
    private String country;
    /**
     * 卖家订单id
     */
    private String merchantOrderId;

    /**
     * 销售渠道
     */
    private String salesChannel;

    /**
     * 明细列表
     */
    private List<PlatformSoOutStockDetailDTO> detailList;

    /**
     * 是否是多渠道订单
     */
    public boolean hasMultiChannel() {
        if (StringUtils.isBlank(this.getSalesChannel())){
            return false;
        }
        return this.getSalesChannel().contains("Non") || this.getPlatformCode().contains("S");
    }
}
