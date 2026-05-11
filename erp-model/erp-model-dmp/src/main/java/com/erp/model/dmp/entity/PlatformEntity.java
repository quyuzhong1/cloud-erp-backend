package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: API平台表
 * @date 2023/1/11 14:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "platform")
@NoArgsConstructor
public class PlatformEntity extends BaseEntity<PlatformEntity> {

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
        super();
        this.setId(kingdee.getCode().toString());
        this.name = kingdee.getDesc();
    }
}
