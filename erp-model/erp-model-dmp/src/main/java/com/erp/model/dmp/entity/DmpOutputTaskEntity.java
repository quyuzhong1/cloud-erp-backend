package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 推送任务
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_output_task")
public class DmpOutputTaskEntity extends BaseEntity<DmpOutputTaskEntity> {

    /**
    * 推送数据配置明细id
    */
    @TableField("output_detail_id")
    private String outputDetailId;
    /**
    * 推送接口条件的开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 推送接口条件的结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
    * 推送状态：init=待推送,finish=已推送,error=推送失败  枚举：DmpOutputTaskStatusEnum
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


    public static final String OUTPUT_DETAIL_ID = "output_detail_id";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String REQUEST_DATA = "request_data";

    public static final String RESPONSE_DATA = "response_data";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
