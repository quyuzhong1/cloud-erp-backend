package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ProductShowDTO
 * @Description TODO
 * @Date 2022-09-17 14:46
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductShowDTO implements Serializable {

    /**
     * 产品id
     */
    private String productId;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品立项状态
     *
     */
    private Integer approvalStatus;
    /**
     * 产品项目状态
     *  0 未启动 1 ;已启动 2 进行中 3 已完成  4 已终止
     *
     */
    private Integer projectStatus;

    /**
     * 截止时间
     */
    private Date endTime;

    /**
     * 项目负责人
     */
    private String projectChargeName;

    /**
     * 产品负责人
     */
    private String productChargeName;

    /**
     * 产品品牌
     */
    private String brandName;

    /**
     * 立项进度
     */
    private Integer approvalProgress;

    /**
     * 项目进度
     */
    private Integer projectProgress;

    /**
     * 任务总数
     */
    private Integer taskCount;


}
