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
 * 拉取任务子状态
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_task_substatus")
public class DmpInputTaskSubStatusEntity extends BaseEntity<DmpInputTaskSubStatusEntity> {

	/**
    * 拉取任务id
    */
    @TableField("main_id")
    private String mainId;
	
    /**
    * 任务状态
    */
    @TableField("status")
    private String status;
    /**
    * 子任务状态，业务开发人员定义
    */
    @TableField("sub_status")
    private String subStatus;


    public static final String STATUS = "status";

    public static final String SUB_STATUS = "sub_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}