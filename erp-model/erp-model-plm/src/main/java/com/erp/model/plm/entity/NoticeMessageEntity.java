package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 通知信息表
 *
 * @TableName notice_message
 */
@TableName(value = "notice_message")
@Data
public class NoticeMessageEntity extends BaseEntity<NoticeMessageEntity> implements Serializable {
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