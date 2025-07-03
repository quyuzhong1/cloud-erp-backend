package com.erp.model.wms.dto.third;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseCancelOutboundReq extends ThirdWarehouseAuth{

    //第三方仓的出库单号
    @NotNull(message = "出库单号不能为空")
    private String orderCode;

    private String erpOrderCode;

    private String warehouseCode;

    private String shopId;

    private String ownerCode;
    //拦截原因
    private String reason;

}
