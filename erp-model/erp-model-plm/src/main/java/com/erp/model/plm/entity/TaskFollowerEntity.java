package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 任务关注的人
 * </p>
 *
 * @author Lambda
 * @since 2023-06-19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("task_follower")
public class TaskFollowerEntity extends BaseEntity<TaskFollowerEntity> {

    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;

    /**
     * 任务id
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 关注的人
     */
    @TableField("user_id")
    private String userId;


    public static final String PRODUCT_ID = "product_id";

    public static final String TASK_ID = "task_id";

    public static final String USER_ID = "user_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
