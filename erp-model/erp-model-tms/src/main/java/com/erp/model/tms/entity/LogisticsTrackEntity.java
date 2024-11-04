package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 物流轨迹表
 * </p>
 *
 * @author zdy
 * @since 2023-11-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_track")
public class LogisticsTrackEntity extends BaseEntity<LogisticsTrackEntity> {

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
    * 状态  运输状态 状态 notFind  查询不到 waitCollect 等待揽收
     * trackIng 运输途中 arriveWaitTake 到达待取
     * deliveryIng 派送途中 deliveryFail 投递失败
     * sign 成功签收 maybeException 可能异常
     * transportLong  运输过久
    */
    @TableField("status")
    private String status;
    /**
    * 内容
    */
    @TableField("content")
    private String content;
    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 运输类型
     */
    @TableField("transport_type")
    private String transportType;
    /**
     * trackNo+content+trackTime的md5
     * DigestUtil.md5Hex
     */
    @TableField("md5")
    private String md5;

    public static final String TRACK_NO = "track_no";

    public static final String TRACK_TIME = "track_time";

    public static final String STATUS = "status";

    public static final String CONTENT = "content";

    @Override
    public Serializable pkVal() {
        return null;
    }

}