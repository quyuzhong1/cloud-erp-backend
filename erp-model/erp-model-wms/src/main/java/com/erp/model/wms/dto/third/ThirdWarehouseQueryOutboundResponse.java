package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseQueryOutboundResponse implements Serializable {

    //第三方中转服务商的发货单号
    private String shippingOrderNo;
    /**
     * 物流单号
     */
    private String trackNo;

}
