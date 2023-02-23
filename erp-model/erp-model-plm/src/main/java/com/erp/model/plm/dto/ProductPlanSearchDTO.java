package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 9:53
 */
@Data
@NoArgsConstructor
public class ProductPlanSearchDTO extends SortDTO {

    /**
     * 类型：0所有，1尚未开始，2已立项，3开发中
     */
    @NotBlank(message = "类型不能为空")
    private String type;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品经理ID
     */
    private List<String> chargeId;

    /**
     * 产品分类ID
     */
    private List<String> categoryId;

    /**
     * 计划调研时间开始
     */
    private LocalDate planSurveyDateBegin;

    /**
     * 计划调研时间结束
     */
    private LocalDate planSurveyDateEnd;

    /**
     * 计划立项时间开始
     */
    private LocalDate planProjectApprovalDateBegin;

    /**
     * 计划立项时间结束
     */
    private LocalDate planProjectApprovalDateEnd;

    /**
     * 计划首批入库时间开始
     */
    private LocalDate planFirstMassStockInDateBegin;

    /**
     * 计划首批入库时间结束
     */
    private LocalDate planFirstMassStockInDateEnd;

    /**
     * 计划上市时间开始
     */
    private LocalDate planListingDateBegin;

    /**
     * 计划上市时间结束
     */
    private LocalDate planListingDateEnd;
}
