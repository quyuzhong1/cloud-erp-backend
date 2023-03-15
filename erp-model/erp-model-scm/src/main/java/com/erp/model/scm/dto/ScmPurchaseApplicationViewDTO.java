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
 * @date 2023/3/15 18:05
 */
@Data
@NoArgsConstructor
public class ScmPurchaseApplicationViewDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 申请单号
     */
    private String code;

    /**
     * 单据状态（待提交，审核中，审核不通过，已审核）
     */
    private String auditStatusName;

    /**
     * 新品首批（false否,true是）
     */
    private Boolean isFirstMassProduct;

    /**
     * 采购单关联状态（未生成，部分生成，已生成）
     */
    private String purchaseRelatedType;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 计划交期
     */
    private Date planDeliveryDate;

    /**
     * 申请数量
     */
    private Integer applyQty;

    /**
     * 实际采购数量
     */
    private Integer realPurchQty;

    /**
     * 签收数量
     */
    private Integer signQty;

    /**
     * 入库数量
     */
    private Integer inStockQty;


}
