package com.erp.model.tms.dto.transfer;

import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author liuruipeng
 * @date 2024年01月24日 18:25
 */
@Data
@AllArgsConstructor
@Builder
public class TransferLogisticsOrderDTO {

    /**
     * 服务商订单号
     */
    private String orderCode;

    /**
     * 参考号
     */
    private String referenceNo;

    /**
     * 跟踪号
     */
    private String trackingNumber;

    /**
     * 订单状态
     */
    private TransferLogisticsStatusEnum orderStatusEnum;

}
