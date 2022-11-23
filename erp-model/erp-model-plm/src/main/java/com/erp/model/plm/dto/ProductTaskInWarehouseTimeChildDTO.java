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
public class ProductTaskInWarehouseTimeChildDTO implements Serializable {

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品状态
     */
    private Integer approvalStatus;

    /**
     * 产品状态名称
     */
    private String approvalStatusName;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 首批量产入库时间(计划上市时间)
     */
    private Date planListingTime;

    /**
     * 产品经理
     */
    private String chargeName;

    /**
     * 时间区间（分组条件）
     */
    private String timeInterval;

}
