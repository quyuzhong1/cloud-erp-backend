package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 按量产入库时间查询显示DTO
 * @date 2022/11/22 19:01
 */
@Data
@NoArgsConstructor
public class ProductTaskInWarehouseViewDTO implements Serializable {

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品状态
     */
    private String productStatus;

    /**
     * 入库时间
     */
    private Date inTime;

    /**
     * 产品经理
     */
    private String productManager;

    /**
     * 是否存在下级（0否，1是）
     */
    private Integer isSubordinate;

    /**
     * 时间区间（分组条件）
     */
    private String timeInterval;

}
