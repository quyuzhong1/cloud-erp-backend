package com.erp.model.plm.dto;

import com.erp.model.plm.vo.ItemMemberVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     */
    private Integer approvalStatus;

    /**
     * 产品立项状态名
     */
    private String approvalStatusName;
    /**
     * 产品项目状态
     * 0 未启动 1 ;已启动 2 进行中 3 已完成  4 已终止
     */
    private Integer projectStatus;

    /**
     * 产品项目状态名
     */
    private String projectStatusName;

    /**
     * 截止时间
     */
    private LocalDate endTime;

    /**
     * 项目负责人
     */
    private String projectChargeName;



    private String projectChargeId;

    /**
     * 产品负责人
     */
    private String productChargeName;

    private String productChargeId;

    /**
     * 产品负责人id
     */
    private List<String> productChargeIdList;

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
    private Boolean ifAddProduct = false;

    /**
     * 是否迭代产品 true  是  false 不是
     */
    private Boolean ifIteration = false;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 总文档数
     */
    private Integer totalDocsCount;


    /**
     * 完成文档档数
     */
    private Integer finishDocsCount;


    /**
     * 迭代数量
     */
    private Integer iterateCount;


    /**
     * 项目阶段
     */
    private String projectPhase;

    /**
     * 项目进展状态值
     */
    private String progressStatus;

    /**
     * 项目进展名
     */
    private String progressStatusName;


    /**
     * 产品属性
     */
    private String property;

    /**
     * 示意图
     */

    private String imageUrl;


    /**
     * 产品分类名
     */
    private String category;


    /**
     * spu no
     *
     */
    private String spuNo;


    /**
     * 项目成员
     */
    private List<ItemMemberVO> itemMemberList;

    private List<Map<String,Object>> itemMember;

}
