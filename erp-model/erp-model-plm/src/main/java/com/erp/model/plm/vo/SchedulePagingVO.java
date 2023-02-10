package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname SchedulePagingVO
 * @Description TODO
 * @Date 2023-02-10 9:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SchedulePagingVO implements Serializable {


    /**
     * 表id
     */
    private String id;


    /**
     * 产品id
     */
    private String productId;


    /**
     * 产品名称
     */
    private String productName;


    /**
     * 阶段
     * projectApproval 立项阶段,
     * project项目阶段,
     * all 所有
     */
    private String phase;

    /**
     * 类型
     * change 变更  initial 初始
     */
    private String type;

    /**
     * 审核任务数
     */
    private Integer taskQuantity;


    /**
     * 提交人id
     */
    private String createUserId;

    /**
     * 提交人
     */
    private String createUserName;


    /**
     * 提交时间
     */
    private String createTime;


    /**
     * 审核完成时间
     */
    private Date approvalFinishTime;

    /**
     * 备注
     */
    private String remark;


    /**
     * 状态
     */
    private String status;

    /**
     * 状态名
     */
    private String statusName;

}
