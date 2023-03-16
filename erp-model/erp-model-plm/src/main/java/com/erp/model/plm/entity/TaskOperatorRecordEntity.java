package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description 产品销售信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:06
 * @param
 * @return
 **/
@TableName(value ="task_operator_log")
@Data
public class TaskOperatorRecordEntity extends BaseEntity implements Serializable {

    /**
     * 任务id
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 操作人id
     */
    @TableField(value = "operator_id")
    private String operatorId;

    /**
     * 操作人名
     */
    @TableField(value = "operator_name")
    private String operatorName;

    /**
     * 操作前状态
     */
    @TableField(value = "before_state")
    private Integer beforeState;

    /**
     * 操作后状态
     */
    @TableField(value = "after_state")
    private Integer afterState;


}