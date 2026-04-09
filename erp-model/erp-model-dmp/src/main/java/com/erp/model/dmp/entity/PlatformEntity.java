package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.model.dmp.enums.PlatformEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.common.core.entity.BaseEntity;

/**
 * @author Will
 * @version 1.0
 * @description: API平台表
 * @date 2023/1/11 14:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value ="platform")
@NoArgsConstructor
public class PlatformEntity extends BaseEntity<PlatformEntity> {

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

    public PlatformEntity(PlatformEnum kingdee) {
        this.id = kingdee.getCode().toString();
        this.name = kingdee.getDesc();
    }
}
