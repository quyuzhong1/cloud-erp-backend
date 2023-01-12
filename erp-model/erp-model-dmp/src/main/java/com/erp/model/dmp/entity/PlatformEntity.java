package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: API平台表
 * @date 2023/1/11 14:43
 */
@Data
@TableName(value ="platform")
public class PlatformEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 平台名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 任务名称
     */
    @TableField(value = "task_name")
    private String taskName;
}
