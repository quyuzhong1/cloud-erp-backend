package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 模板任务与sku字段配置关系表(TemplateTaskRefSkuConfig)实体类
 *
 * @author yl
 * @since 2022-11-24 16:15:43
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("template_task_ref_sku_config")
public class TemplateTaskRefSkuConfigEntity extends BaseEntity<TemplateTaskRefSkuConfigEntity> implements Serializable {
    private static final long serialVersionUID = 443047567565109376L;

    /**
     * 任务id
     */
    private String taskId;
    /**
     * 字段配置类型 createSku 创造sku，fillProductInfo 填写信息
     */
    private String fieldConfigType;
    /**
     * 勾选字段后的json 字段
     */
    private String fieldJson;
    /**
     * 模板id
     */
    private String templateId;

}

