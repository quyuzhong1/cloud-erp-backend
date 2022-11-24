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
     * id（用于前端展示）
     */
    private Integer id;

    /**
     * 父级id（用于前端展示）
     */
    private Integer parentId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品状态
     */
    private Integer status;

    /**
     * 产品状态名称
     */
    private String statusName;

    /**
     * 是否是项目状态
     */
    private Integer isProjectStatus;

    /**
     * 项目状态
     */
    private Integer projectStatus;

    /**
     * 项目状态名称
     */
    private String projectStatusName;

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
