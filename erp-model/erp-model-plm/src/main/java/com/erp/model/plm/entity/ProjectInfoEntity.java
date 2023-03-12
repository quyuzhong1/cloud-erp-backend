package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

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
public class ProjectInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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
    private LocalDateTime startTime;

    /**
     * 计划结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
      *项目状态 0 未启动 1 ;已启动 2 进行中 3 已完成  4 已终止
     */
    @TableField("project_status")
    private Integer projectStatus;

    /**
     * 项目描述
     */
    @TableField("describe")
    private String describe;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
