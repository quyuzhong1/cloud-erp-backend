package com.sdk.tms.track123.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.dto.UniqueDto;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * FBA货件DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @author Jim
 * @date 2023/11/1
 **/
@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class PlatformTrackDTO extends UniqueDto {
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

    @Override
    public String toString() {
        return "PlatformTrackDTO{" +
                "trackNo='" + trackNo + '\'' +
                ", trackTime=" + trackTime +
                ", status='" + status + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
