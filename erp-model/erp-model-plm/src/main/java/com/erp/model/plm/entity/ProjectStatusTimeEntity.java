package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:33
 */
@TableName(value ="project_status_time")
@Data
@NoArgsConstructor
public class ProjectStatusTimeEntity extends BaseEntity {

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 项目状态
     */
    @TableField(value = "status")
    private String status;

    /**
     * 状态更新时间
     */
    @TableField(value = "status_time")
    private LocalDateTime statusTime;

}
