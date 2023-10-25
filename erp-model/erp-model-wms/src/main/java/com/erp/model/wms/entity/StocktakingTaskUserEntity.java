package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 盘点任务 盘点人表
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("stocktaking_task_user")
public class StocktakingTaskUserEntity extends BaseEntity<StocktakingTaskUserEntity> {

    /**
     * 盘点人
     */
    @TableField("user_id")
    private String userId;

    /**
     * 盘点人名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;


    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String STOCKTAKING_TASK_ID = "stocktaking_task_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
