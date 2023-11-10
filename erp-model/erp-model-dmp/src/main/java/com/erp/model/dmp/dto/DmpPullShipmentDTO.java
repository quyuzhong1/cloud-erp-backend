package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 拉取货件信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmpPullShipmentDTO {
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 货件单号集合
     */
    private List<String> shipmentCodeList;
}