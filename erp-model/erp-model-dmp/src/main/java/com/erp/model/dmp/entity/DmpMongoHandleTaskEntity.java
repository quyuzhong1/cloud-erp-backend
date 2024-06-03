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
 * 处理mongo业务数据任务
 * </p>
 *
 * @author Jim
 * @since 2024-05-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_mongo_handle_task")
public class DmpMongoHandleTaskEntity extends BaseEntity<DmpMongoHandleTaskEntity> {

    /**
    * 业务处理类型 DmpMongoHandleTypeEnum
    */
    @TableField("handle_type")
    private String handleType;
    /**
    * 业务处理类型名称
    */
    @TableField("handle_type_name")
    private String handleTypeName;
    /**
    * 任务每次处理的数量
    */
    @TableField("handle_count")
    private Integer handleCount;
    /**
    * 最后处理的mongo主键ID
    */
    @TableField("last_id")
    private String lastId;
    /**
    * 下次处理执行时间
    */
    @TableField("next_time")
    private LocalDateTime nextTime;
    /**
    * 执行时间间隔，单位描述
    */
    @TableField("interval_time")
    private Integer intervalTime;


    public static final String HANDLE_TYPE = "handle_type";

    public static final String HANDLE_TYPE_NAME = "handle_type_name";

    public static final String HANDLE_COUNT = "handle_count";

    public static final String LAST_ID = "last_id";

    public static final String NEXT_TIME = "next_time";

    public static final String INTERVAL_TIME = "interval_time";

}