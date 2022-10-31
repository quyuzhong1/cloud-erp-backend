package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductKeyDTO {
    private String id;
    private String skuId;
    private String purchaseId;
    private String costId;
    private String saleId;
    private String packId;
    private String logisticsId;
}
