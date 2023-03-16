package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 16:47
 */
@Data
@NoArgsConstructor
public class ScmSalesDemandPagingParamDTO extends SortDTO {

    /**
     * 审核状态
     */
    private List<String> approveStatusList;

    /**
     * 单据编号
     */
    private String code;

    /**
     * 是否加急（false否，true是）
     */
    private Boolean isUrgent;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 目的仓库id
     */
    private List<String> destWarehouseIdList;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 作废状态（0未作废，1已作废）
     */
    private List<String> invalidStatus;

    /**
     * 创建人
     */
    private List<String> createUserIdList;

    /**
     * 创建时间开始
     */
    private LocalDateTime createTimeBegin;

    /**
     * 创建时间结束
     */
    private LocalDateTime createTimeEnd;

    /**
     * 计划交期开始
     */
    private LocalDate planDeliveryDateBegin;

    /**
     * 计划交期结束
     */
    private LocalDate planDeliveryDateEnd;

    /**
     * 审核时间开始
     */
    private LocalDateTime approvePassTimeBegin;

    /**
     * 审核时间结束
     */
    private LocalDateTime approvePassTimeEnd;
}
