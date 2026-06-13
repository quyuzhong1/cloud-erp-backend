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
@TableName("tms_async_task_detail")
public class TmsAsyncTaskDetailEntity extends BaseEntity<TmsAsyncTaskDetailEntity> {

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
    * 状态：pending=待认领, ing=执行中, finish=已完成, failed=失败
    */
    @TableField("status")
    private String status;
    /**
    * 错误信息
    */
    @TableField("error_data")
    private String errorData;

    // -------------------------------------------------------
    // 新增字段（refactor-tms-async-task-engine）
    // -------------------------------------------------------

    /**
     * 明细防重键，格式建议：methodType:businessId[:extraParams]；存量明细默认值为空不参与防重
     */
    @TableField("detail_unique_key")
    private String detailUniqueKey;

    /**
     * 明细独立执行参数 JSON；与主任务 data_json 不同的单条字段存此处；不需要时为 null
     */
    @TableField("detail_param_json")
    private String detailParamJson;

    // -------------------------------------------------------
    // 列名常量
    // -------------------------------------------------------

    public static final String MAIN_ID = "main_id";
    public static final String BUSINESS_ID = "business_id";
    public static final String BUSINESS_CODE = "business_code";
    public static final String BUSINESS_TYPE = "business_type";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String STATUS = "status";
    public static final String ERROR_DATA = "error_data";
    public static final String DETAIL_UNIQUE_KEY = "detail_unique_key";
    public static final String DETAIL_PARAM_JSON = "detail_param_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
