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
     * 表格类型（产品经理、产品等级、产品分类）
     */
    private String tableType;

    /**
     * 组别
     */
    private String deptName;

    /**
     * 姓名
     */
    private String chargeName;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品分类
     */
    private String category;


    /**
     * 总规划数
     */
    private Long totalCount;

    /**
     * 立项数量
     */
    private Long approvalCount;

    /**
     * 进行中数量
     */
    private Long progressCount;

    /**
     * 完成数量
     */
    private Long completeCount;

    /**
     * 延期数量
     */
    private Long approvalDelayCount;

    /**
     * 取消数量
     */
    private Long cancelCount;
}
