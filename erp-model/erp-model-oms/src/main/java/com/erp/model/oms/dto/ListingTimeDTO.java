package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ListingTimeDTO implements Serializable {
    /**
     * skuId
     */
    private String skuId;

    /**
     * 销售日期
     */
    private LocalDate soDate;
}
