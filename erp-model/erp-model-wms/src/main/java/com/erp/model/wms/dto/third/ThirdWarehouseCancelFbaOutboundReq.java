package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
public class ThirdWarehouseCancelFbaOutboundReq extends ThirdWarehouseAuth{

    //第三方仓的出库单号
    @NotNull(message = "出库单号不能为空")
    private String orderCode;

    private String erpOrderCode;

    private String sourceId;
    /**
     * 冗余字段，记录中台唯一标识
     */
    private String sourceCode;
    private String soCode;

}
