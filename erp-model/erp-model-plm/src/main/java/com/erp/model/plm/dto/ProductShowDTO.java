package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 项目id
     */
    private String projectId;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品等级id
     */
    private String gradeId;

    /**
     * 产品类型
     */
    private Integer type;

    /**
     * 产品立项状态
     *
     */
    private Integer approvalStatus;

    /**
     * 产品立项状态名
     *
     */
    private String approvalStatusName;
    /**
     * 产品项目状态
     *  0 未启动 1 ;已启动 2 进行中 3 已完成  4 已终止
     *
     */
    private Integer projectStatus;

    /**
     * 产品项目状态名
     *
     */
    private String projectStatusName;

    /**
     * 截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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
     * 立项任务总数
     */
    private Integer approvalTaskCount;

    /**
     * 立项任务完成总数
     */
    private Integer approvalFinishTaskCount;

    /**
     * 项目任务总数
     */
    private Integer projectTaskCount;

    /**
     * 项目任务完成总数
     */
    private Integer projectFinishTaskCount;




    /**
     * 立项进度
     */
    private double approvalProgress;

    /**
     * 项目进度
     */
    private double projectProgress;

    /**
     * 任务总数
     */
    private Integer taskCount;


    /**
     * 是否收藏 产品 true  收藏 false 没有
     */
    private Boolean ifAddProduct=false;

    /**
     * 是否迭代产品 true  是  false 不是
     */
    private Boolean ifIteration=false;


}
