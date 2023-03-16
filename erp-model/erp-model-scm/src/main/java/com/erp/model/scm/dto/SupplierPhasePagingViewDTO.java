package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商阶段分页展示数据
 * @author Lambda
 * @Classname SupplierPhasePagingViemDTO
 * @Description TODO
 * @Date 2023-03-16 12:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPhasePagingViewDTO implements Serializable {

    /**
     * 供应商 阶段表 id
     */
    private String id;


    /**
     * 供应商表id
     */
    private String SupplierId;


    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 分类名
     */
    private String categoryName;

    /**
     * 操作类型
     */
    private String type;

    /**
     * 当前阶段
     */
    private String currentPhase;


    /**
     * 目标阶段
     */
    private String targetPhase;

    /**
     * 说明
     */
    private String description;

    /**
     * 审核状态
     */
    private String approveStatus;

    /**
     * 创建人id
     */
    private String createUserName;

    /**
     * 审核人
     */
    private String approvedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
