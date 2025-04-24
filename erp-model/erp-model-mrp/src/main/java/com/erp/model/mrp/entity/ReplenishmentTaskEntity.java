package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 补货建议同步任务表
 * </p>
 *
 * @author liaohui
 * @since 2024-10-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("replenishment_task")
@EqualsAndHashCode(callSuper = true)
public class ReplenishmentTaskEntity extends BaseEntity<ReplenishmentTaskEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_id")
    private String replenishmentId;

    /**
     * 状态-1 待更新 2 更新中，3更新成功，4更新失败
     */
    @TableField("status")
    private String status;

    /**
     * 更新返回信息（错误信息和成功信息）
     */
    @TableField("return_msg")
    private String returnMsg;


    public static final String REPLENISHMENT_ID = "replenishment_id";

    public static final String STATUS = "status";

    public static final String RETURN_MSG = "return_msg";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
