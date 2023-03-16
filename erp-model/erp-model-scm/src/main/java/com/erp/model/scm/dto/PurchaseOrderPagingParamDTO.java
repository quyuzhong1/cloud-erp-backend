package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 11:15
 */
@Data
@NoArgsConstructor
public class PurchaseOrderPagingParamDTO extends SortDTO {

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 供应商id
     */
    private List<String> supplierIdList;

    /**
     * 审核状态
     */
    private List<String> approveStatusList;

    /**
     * 作废状态（0未作废，1已作废）
     */
    private String invalidStatus;

    /**
     * 到货状态（0未到货，1部分到货，2已到货）
     */
    private List<String> arrivalStatusList;

    /**
     * 是否加急（false否，true是）
     */
    private Boolean isUrgent;

    /**
     * 交货仓库id
     */
    private List<String> deliveryWarehouseIdList;

    /**
     * 新品首批（false否,true是）
     */
    private Boolean isFirstMassProduct;

    /**
     * 创建时间开始
     */
    private LocalDateTime createTimeBegin;

    /**
     * 创建时间结束
     */
    private LocalDateTime createTimeEnd;

    /**
     * 审核时间开始
     */
    private LocalDateTime approveTimeBegin;

    /**
     * 审核时间结束
     */
    private LocalDateTime approveTimeEnd;

    /**
     * 申请人id
     */
    private List<String> purchaseUserIdList;

    /**
     * 创建人id
     */
    private List<String> createUserIdList;
}
