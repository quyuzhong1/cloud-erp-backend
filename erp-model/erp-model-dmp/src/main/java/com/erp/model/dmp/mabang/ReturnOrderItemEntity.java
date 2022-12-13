package com.erp.model.dmp.mabang;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class ReturnOrderItemEntity {
    private String pictureUrl;
    private String productUnit;
    private Integer quantity;
    private Integer quantity1;
    private BigDecimal sellPrice;
    private String specifics;
    private Integer status;
    private String stockGrid;
    private String stockSku;
    private Integer stockWarehouseId;
    private String title;
    private Integer quantity2;
    @SerializedName("inspection_time")
    private String inspectionTime;
    private String stockWarehouseName;
}
