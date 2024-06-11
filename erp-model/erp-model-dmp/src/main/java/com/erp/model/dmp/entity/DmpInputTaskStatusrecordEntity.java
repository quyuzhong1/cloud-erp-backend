package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 拉取任务状态记录
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_task_statusrecord")
public class DmpInputTaskStatusrecordEntity extends BaseEntity<DmpInputTaskStatusrecordEntity> {

    /**
    * 任务状态
    */
    @TableField("status")
    private String status;


    public static final String STATUS = "status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}