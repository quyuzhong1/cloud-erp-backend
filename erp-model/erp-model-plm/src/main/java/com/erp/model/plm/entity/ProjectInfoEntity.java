package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 产品项目表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_info")
public class ProjectInfoEntity extends BaseEntity<ProjectInfoEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("product_id")
    private String productId;

    @TableField("name")
    private String name;

    /**
     *
     * 项目负责人id
     */
    @TableField("charge_id")
    private String chargeId;

    /**
     *项目负责人
     */
    @TableField("charge_name")
    private String chargeName;

    /**
     * 项目计划开始时间
     */
    @TableField("start_time")
    private LocalDate startTime;

    /**
     * 计划结束时间
     */
    @TableField("end_time")
    private LocalDate endTime;

    /**
      *项目状态 0 未启动 1 ;已启动 2 进行中 3 已完成  4 已中止(表式 已暂停) 5 终止
     */
    @TableField("project_status")
    private Integer projectStatus;

    /**
     *项目状态  0 未启动 1 ;已启动 2 进行中 3 已完成  4 已中止(表式 已暂停) 5 终止
     * 暂停前的状态
     */
    @TableField("suspend_before_status")
    private Integer suspendBeforeStatus;

    /**
     * 项目描述
     */
    @TableField("describe")
    private String describe;

    /**
     * 项目启动日期
     */
    @TableField("project_launch_date")
    private LocalDate projectLaunchDate;

    /**
     * 项目结项日期
     */
    @TableField("project_finish_date")
    private LocalDateTime projectFinishDate;
}
