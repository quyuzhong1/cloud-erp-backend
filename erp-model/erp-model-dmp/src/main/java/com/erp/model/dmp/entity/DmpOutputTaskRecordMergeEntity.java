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
 * 推送任务记录合并表
 * </p>
 *
 * @author Luo_WG
 * @since 2025-02-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_output_task_record_merge")
public class DmpOutputTaskRecordMergeEntity extends BaseEntity<DmpOutputTaskRecordMergeEntity> {

    /**
    * 主id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 合并id
    */
    @TableField("merge_id")
    private String mergeId;
    /**
    * waitMerge待合并，merge已合并
    */
    @TableField("merge_status")
    private String mergeStatus;


    public static final String MAIN_ID = "main_id";

    public static final String MERGE_ID = "merge_id";

    public static final String MERGE_STATUS = "merge_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}