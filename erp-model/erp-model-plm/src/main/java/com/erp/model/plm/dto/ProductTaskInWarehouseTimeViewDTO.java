package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 按量产入库时间查询显示DTO
 * @date 2022/11/22 19:01
 */
@Data
@NoArgsConstructor
public class ProductTaskInWarehouseTimeViewDTO implements Serializable {

    /**
     * 时间区间（分组条件）
     */
    private String timeInterval;

    /**
     * 子集
     */
    private List<ProductTaskInWarehouseTimeChildDTO> childrenList;
}
