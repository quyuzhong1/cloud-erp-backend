package com.erp.model.wms.dto.third.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThirdWarehouseCancelOutboundReq {

    //第三方仓的出库单号
    private String orderCode;

    //拦截原因
    private String reason;

}
