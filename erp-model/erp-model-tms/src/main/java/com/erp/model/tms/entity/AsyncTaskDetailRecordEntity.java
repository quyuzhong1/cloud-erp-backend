package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 异步任务记录明细
 * </p>
 *
 * @author jack
 * @since 2026-01-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("async_task_detail_record")
public class AsyncTaskDetailRecordEntity extends BaseEntity<AsyncTaskDetailRecordEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 单据id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 单据编码
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 单据类型
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
    * 状态：success=成功,  failed=失败  枚举：AsyncTaskDetailRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * json
    */
    @TableField("error_data")
    private String errorData;


    public static final String MAIN_ID = "main_id";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String ERROR_DATA = "error_data";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
