package com.erp.wms.aliexpress.model.order;

import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AliexpressCancelOrderDTO {

    private AliexpressAuthDTO aliexpressAuthDTO;

    private String orderType;

    private String ownerCode;

    private String orderId;

    private String orderCode;

    private String warehouseCode;
}