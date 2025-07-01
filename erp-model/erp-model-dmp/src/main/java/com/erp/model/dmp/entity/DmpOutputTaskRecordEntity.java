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
     * 主id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 数据id
     */
    @TableField("data_id")
    private String dataId;

    /**
     * 推送编号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 推送状态：init=待推送,finish=推送成功,error=推送失败  枚举：DmpOutputTaskRecordStatusEnum
     */
    @TableField("status")
    private String status;

    /**
     * 推送报文
     */
    @TableField("request_data")
    private String requestData;
    /**
     * 响应报文
     */
    @TableField("response_data")
    private String responseData;

    /**
     * 响应报文
     */
    @TableField("error_count")
    private Integer errorCount;

    /**
     * 是否需要同步 true 同步 false 无需同步
     */
    @TableField("is_need_sync")
    private Boolean isNeedSync;

    @TableField(exist = false)
    private String sourceId;

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
