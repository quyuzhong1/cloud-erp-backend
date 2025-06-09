package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 任务阶段表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_phase")
public class ProjectPhaseEntity extends BaseEntity<ProjectPhaseEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("product_id")
    private String productId;

    /**
     * 阶段名
     */
    @TableField("name")
    private String name;

    /**
     * 顺序
     * 必须有值 且 同产品 这个序号不同
     */
    private Integer seq;

     //是否来源系统
    @TableField("is_source_sys")
    private Integer isSourceSys;

}
