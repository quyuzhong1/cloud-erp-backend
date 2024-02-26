package com.erp.model.wms.dto;

import lombok.Builder;
import lombok.Data;

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
public class SupplierCountDTO implements Serializable {
    int count;
    LocalDate localDate;
}
