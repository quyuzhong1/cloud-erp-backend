package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 任务关注的人
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("template_task_concern")
public class TemplateTaskConcernEntity extends BaseEntity<TemplateTaskConcernEntity> {


    /**
    * 模板
    */
    @TableField("template_id")
    private String templateId;

    /**
    * 任务id
    */
    @TableField("template_task_id")
    private String templateTaskId;

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