package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 11:17
 */
@Data
@NoArgsConstructor
public class ProductPlanGroupVO implements Serializable {

    /**
     * 组别
     */
    private String deptName;

    /**
     * 产品经理ID
     */
    private String chargeId;

    /**
     * 产品经理
     */
    private String chargeName;

    /**
     * 等级ID
     */
    private String gradeId;

    /**
     * 等级
     */
    private String grade;

    /**
     * 类目ID
     */
    private String categoryId;

    /**
     * 类目
     */
    private String category;

    /**
     * 总规划数
     */
    private Integer totalCount;

    /**
     * 立项数量
     */
    private Integer approvalCount;

    /**
     * 进行中数量
     */
    private Integer progressCount;

    /**
     * 完成数量
     */
    private Integer completeCount;

    /**
     * 延期数量
     */
    private Integer approvalDelayCount;

    /**
     * 取消数量
     */
    private Integer cancelCount;
}
