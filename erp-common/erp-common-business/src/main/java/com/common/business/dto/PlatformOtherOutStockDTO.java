package com.common.business.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 其他出库DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 * {@code @Date} 2024/03/06
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformOtherOutStockDTO extends UniqueDto {

    /**
     * 平台多渠道订单号
     */
    private String platformCode;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 平台配送时间
     */
    private String platformDeliveryTime;

    /**
     * 明细列表
     */
    private List<PlatformOtherOutStockDetailDTO> detailList;

    public PlatformOtherOutStockDTO(String platformCode,
                                    String shopId,
                                    String platformDeliveryTime,
                                    List<PlatformOtherOutStockDetailDTO> detailList,
                                    String uniqueId,
                                    String platform
    ) {
        this.platformCode = platformCode;
        this.shopId = shopId;
        this.platformDeliveryTime = platformDeliveryTime;
        this.detailList = detailList;
        super.setUniqueId(uniqueId);
        super.setPlatform(platform);
    }
}
