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
 * 异步任务记录
 * </p>
 *
 * @author jack
 * @since 2026-01-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("async_task_record")
public class AsyncTaskRecordEntity extends BaseEntity<AsyncTaskRecordEntity> {

    /**
    * 单据名称
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
    * 状态：success=成功,part_success=部分成功,  failed=失败  枚举：AsyncTaskRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * json
    */
    @TableField("data_json")
    private String dataJson;
    /**
     *
     */
    @TableField("error_data")
    private String errorData;
    /**
     *明细任务数量
     */
    @TableField("detail_count")
    private Integer detailCount;

    public static final String BUSINESS_TYPE = "business_type";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String DATA_JSON = "data_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
