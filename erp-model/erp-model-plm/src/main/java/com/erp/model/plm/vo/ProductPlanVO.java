package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 9:46
 */
@Data
@NoArgsConstructor
public class ProductPlanVO implements Serializable {

    /**
     * 规划id
     */
    private String id;

    /**
     * 年份
     */
    private Integer  year;

    /**
     * 产品示意图URL
     */
    private String imageUrl;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品状态
     */
    private String productStatus;

    /**
     * 产品状态名称
     */
    private String productStatusName;

    /**
     * 产品分类
     */
    private String category;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品经理
     */
    private String chargeName;

    /**
     * 计划调研时间
     */
    private LocalDate planSurveyDate;

    /**
     * 计划立项时间
     */
    private LocalDate planProjectApprovalDate;

    /**
     * 计划首批入库时间
     */
    private LocalDate planFirstMassStockInDate;

    /**
     * 计划上市时间
     */
    private LocalDate planListingDate;

    /**
     * 实际立项时间
     */
    private LocalDate projectApprovalDate;

    /**
     * 实际首批入库时间
     */
    private LocalDate firstMassStockInDate;

    /**
     * 实际上市时间
     */
    private LocalDate listingDate;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDate createTime;

    /**
     * 调研是否延期
     */
    private Boolean isDelaySurvey;

    /**
     * 立项是否延期
     */
    private Boolean isDelayApproval;
}
