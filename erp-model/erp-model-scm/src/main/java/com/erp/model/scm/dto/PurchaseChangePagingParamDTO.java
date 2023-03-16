package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 12:04
 */
@Data
@NoArgsConstructor
public class PurchaseChangePagingParamDTO extends SortDTO {


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
     * 作废状态
     */
    private String invalidStatus;

    /**
     * 交货仓库id
     */
    private List<String> deliveryWarehouseIdList;

    /**
     * 创建时间开始
     */
    private LocalDate createTimeBegin;

    /**
     * 创建时间结束
     */
    private LocalDate createTimeEnd;

    /**
     * 审核时间开始
     */
    private LocalDate approveTimeBegin;

    /**
     * 审核时间结束
     */
    private LocalDate approveTimeEnd;

    /**
     * 创建人id
     */
    private List<String> createUserIdList;
}