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
 * 推送任务记录
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_output_task_record")
public class DmpOutputTaskRecordEntity extends BaseEntity<DmpOutputTaskRecordEntity> {

    /**
    * 输入任务存储状态（冗余）
    */
    @TableField("input_status")
    private String inputStatus;
    /**
    * 数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名（冗余）
    */
    @TableField("storage_name")
    private String storageName;
    /**
    * 数据id
    */
    @TableField("data_id")
    private String dataId;
    /**
    * 推送状态：init=待推送,finish=推送成功,error=推送失败  枚举：DmpOutputTaskRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 异常原因
    */
    @TableField("error_message")
    private String errorMessage;


    public static final String INPUT_STATUS = "input_status";

    public static final String STORAGE_NAME = "storage_name";

    public static final String DATA_ID = "data_id";

    public static final String STATUS = "status";

    public static final String ERROR_MESSAGE = "error_message";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
