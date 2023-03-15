package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 模板阶段表
 * @author Administrator
 * @TableName template_phase
 */
@Data
@TableName(value ="template_phase")
public class TemplatePhaseEntity  extends BaseEntity {


    /**
     * 阶段名
     */
    private String name;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    private Integer seq;

    /**
     * 模板id
     */
    private String templateId;

    private static final long serialVersionUID = 1L;


}