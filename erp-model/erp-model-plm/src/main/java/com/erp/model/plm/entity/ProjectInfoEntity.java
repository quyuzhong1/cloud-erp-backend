package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

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



    @TableField("product_name")
    private String productName;


    //对应的id
    @TableField("flag_id")
    private String flagId;

    //来源类型 0 新建  1  项目复制  2，模板
    @TableField("source_type")
    private Integer sourceType;

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
    private Date startTime;

    /**
     * 计划结束时间
     */
    @TableField("end_time")
    private Date endTime;

    //项目状态 0 未启动 1 ;已启动 2 进行中 3 已完成  4 已终止
    @TableField("project_status")
    private Integer projectStatus;

    /**
     * 项目描述
     */
    @TableField("describe")
    private String describe;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
