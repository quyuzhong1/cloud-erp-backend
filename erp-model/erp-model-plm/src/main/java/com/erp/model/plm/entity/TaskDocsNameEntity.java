package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Classname TaskDocesNameEntity

 * @Date 2022-09-22 12:08
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_docs_name")
public class TaskDocsNameEntity extends BaseEntity<TaskDocsNameEntity> implements Serializable {

    @TableField("name")
    private String name;

    @TableField("product_id")
    private String productId;

}
