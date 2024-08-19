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
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_logistics_track")
public class DmpLogisticsTrackEntity extends BaseEntity<DmpLogisticsTrackEntity> {

    /**
    * 承运商编码
    */
    @TableField("courier_code")
    private String courierCode;
    /**
    * 承运商名称
    */
    @TableField("courier_name")
    private String courierName;
    /**
    * 运单号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 运单时间
    */
    @TableField("track_time")
    private LocalDateTime trackTime;
    /**
    * 状态 notFind  查询不到 waitCollect 等待揽收trackIng 运输途中 arriveWaitTake 到达待取deliveryIng 派送途中 deliveryFail 投递失败
sign 成功签收 maybeException 可能异常transportLong  运输过久
    */
    @TableField("status")
    private String status;
    /**
    * 内容
    */
    @TableField("content")
    private String content;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 运输类型，expressDelivery快递，ocean海运，aviation空运
    */
    @TableField("transport_type")
    private String transportType;


    public static final String COURIER_CODE = "courier_code";

    public static final String COURIER_NAME = "courier_name";

    public static final String TRACK_NO = "track_no";

    public static final String TRACK_TIME = "track_time";

    public static final String STATUS = "status";

    public static final String CONTENT = "content";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}