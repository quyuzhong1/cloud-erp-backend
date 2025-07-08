package com.erp.model.wms.dto.third;

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
public class ThirdWarehouseCancelInboundReq {

    //第三方仓的入库单号
    private String receivingCode;

    //erp单号
    private String sourceCode;
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 货主编码
     */
    private String ownerCode;
    private String warehouseCode;
}
