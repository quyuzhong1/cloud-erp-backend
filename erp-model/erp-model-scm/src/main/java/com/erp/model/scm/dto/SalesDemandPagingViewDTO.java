package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 16:42
 */
@Data
@NoArgsConstructor
public class SalesDemandPagingViewDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 备货编号
     */
    private String code;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 是否加急（false否，true是）
     */
    private Boolean isUrgent;

    /**
     * 新品首批（false否,true是）
     */
    private Boolean isFirstMassProduct;

    /**
     * 计划交期
     */
    private Date planDeliveryDate;

    /**
     * 计划备货数量
     */
    private Integer planStockQty;

    /**
     * 目的仓库id
     */
    private String destWarehouseId;

    /**
     * 目的仓库名称
     */
    private String destWarehouseName;

    /**
     * 备注
     */
    private String remark;
}
