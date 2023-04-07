package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import org.apache.ibatis.annotations.Result;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 通知节点表
 *
 * @TableName notice_node
 */
@TableName(value = "notice_node")
@Data
public class NoticeNodeEntity extends BaseEntity implements Serializable {
    /**
     * 节点名称
     */
    @TableField(value = "node_name")
    private String nodeName;

    /**
     * 节点标志
     */
    private String nodeFlag;

    /**
     * 是否已添加 1 已添加 0  没有
     */
    private Integer existAdd;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}