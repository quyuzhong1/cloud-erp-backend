package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname TaskDocesNameEntity
 * @Description TODO
 * @Date 2022-09-22 12:08
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_docs_name")
public class TaskDocsNameEntity extends BaseEntity implements Serializable {

    @TableField("name")
    private String name;

    @TableField("product_id")
    private String productId;

}
