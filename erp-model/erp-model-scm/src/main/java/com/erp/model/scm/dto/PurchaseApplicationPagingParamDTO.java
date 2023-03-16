package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 18:05
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationPagingParamDTO extends SortDTO {

    /**
     * 审核状态
     */
    private List<String> approveStatusList;

    /**
     * 采购订单生成状态（0未生成，1部分生成，2已生成
     */
    private List<Boolean> createPoTypeList;

    /**
     * 新品首批（false否,true是）
     */
    private Boolean isFirstMassProduct;

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
     * 计划交期开始
     */
    private LocalDate planDeliveryDateBegin;

    /**
     * 计划交期结束
     */
    private LocalDate planDeliveryDateEnd;

    /**
     * 目的仓库id
     */
    private List<String> destWarehouseIdList;

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
     * 申请人id
     */
    private List<String> applyUserIdList;

    /**
     * 创建人id
     */
    private List<String> createUserIdList;


}
