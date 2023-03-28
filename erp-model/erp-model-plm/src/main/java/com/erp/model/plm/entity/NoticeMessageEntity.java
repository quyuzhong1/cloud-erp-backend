package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 通知信息表
 *
 * @TableName notice_message
 */
@TableName(value = "notice_message")
@Data
public class NoticeMessageEntity extends BaseEntity implements Serializable {
    /**
     * 通知节点id
     */
    private String nodeId;

    /**
     *项目人员
     */
    private String itemPeople;

    /**
     * 其它人员
     */
    private String otherPeople;

    /**
     * 通知状态 1 已开启  0  未开启
     */
    private Integer state;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}