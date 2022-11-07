package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.annotations.Result;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知节点表
 *
 * @TableName notice_node
 */
@TableName(value = "notice_node")
@Data
public class NoticeNodeEntity implements Serializable {
    /**
     *
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否已添加 1 已添加 0  没有
     */
    private Integer existAdd;

    /**
     * 创建人
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 更新人
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}