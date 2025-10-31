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
 * DMP飞书变更实例IDS记录
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_feishu_instance_ids")
public class DmpFeishuInstanceIdsEntity extends BaseEntity<DmpFeishuInstanceIdsEntity> {

    /**
    * 任务转换ID
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 店铺ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 任务来源唯一加密代号
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 任务数据加密代号
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 飞书实例ID
    */
    @TableField("instance_id")
    private String instanceId;
    /**
    * ERP实例代号
    */
    @TableField("ulanzi_approval_code")
    private String ulanziApprovalCode;


    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String INSTANCE_ID = "instance_id";

    public static final String ULANZI_APPROVAL_CODE = "ulanzi_approval_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}