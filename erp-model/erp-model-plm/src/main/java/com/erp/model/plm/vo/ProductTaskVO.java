package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname 产品任务
 * @Description TODO
 * @Date 2023-02-02 19:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductTaskVO implements Serializable {

    /**
     * id（用于前端展示）
     */
    private Integer id;


    private String productName;

    /**
     * 父级id（用于前端展示）
     */
    private Integer parentId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 项目id
     */
    private String projectId;


    /**
     * 父级id
     */
    private String pid;

    /**
     * 产品id
     */
    private String productId;


    /**
     * 任务名
     */
    private String name;


    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    private Integer type;

    /**
     * 负责人id
     */
    private String chargeId;


    /**
     * 负责人名
     */
    private String chargeName;

    /**
     * 前置任务id
     */
    private List<String> preTaskIdList;


    /**
     * 前置任务名
     */
    private String preTaskNames;

    /**
     * 计划开始时间
     */
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    private Date planEndTime;


    /**
     * 实际开始时间
     */
    private Date realityStartTime;


    /**
     * 实际结束时间
     */
    private Date realityEndTime;


    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    private Integer priority;

    /**
     * 任务阶段id
     */
    private String phaseId;

    /**
     * 任务阶段名
     */
    private String phaseName;


    /**
     * 任务描述
     */
    private String description;


    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 任务状态名
     */
    private String statusName;

    /**
     * 任务排期状态
     */
    private String scheduleStatus;

    /**
     * 任务排期状态名
     */
    private String scheduleStatusName;

    /**
     * 排期类型
     */
    private String scheduleType;



    /**
     * 设置里程碑(0否，1是)
     */
    private Integer isMilepost;


    /**
     * 关联sku 表id集合
     */
    private List<String> refSkuIdList;

    /**
     * 关联sku 表sku 名字集合
     */
    private List<String> refSkuNoList;


    /**
     * 交付文档名称
     */
    private String deliveryDocsNames;


    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 创建人
     */
    private String createUserName;


    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 更新人
     */
    private String updateUserName;


    /**
     * 是否变更中
     */

    private Boolean isChange = false;


}
