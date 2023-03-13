package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

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


    private Integer seq;

    /**
     * 模板id
     */
    private String templateId;

    private static final long serialVersionUID = 1L;


}