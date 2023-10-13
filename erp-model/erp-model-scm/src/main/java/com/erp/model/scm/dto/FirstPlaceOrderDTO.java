package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FirstPlaceOrderDTO implements Serializable {
    /**
     * skuId
     */
    private String skuId;

    /**
     * 采购日期
     */
    private LocalDate purchaseDate;
}
