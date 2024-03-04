package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author zdy
 * @ClassName SupplierCountDTO
 * @description: TODO
 * @date 2024年02月26日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierCountDTO implements Serializable {
    private int count;
    private LocalDate localDate;
}
