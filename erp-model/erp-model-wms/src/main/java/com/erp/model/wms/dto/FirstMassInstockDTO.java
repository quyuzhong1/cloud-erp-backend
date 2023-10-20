package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FirstMassInstockDTO implements Serializable {
    /**
     * skuId
     */
    private String skuId;

    /**
     * 入库日期
     */
    private LocalDate firstMassProductDate;
}
