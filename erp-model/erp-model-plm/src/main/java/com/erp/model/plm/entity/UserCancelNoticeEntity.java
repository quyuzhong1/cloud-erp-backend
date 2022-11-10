package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname UserCancelNoticeEntity
 * @Description TODO
 * @Date 2022-11-10 15:01
 * @Created by yl
 */
@TableName(value = "user_cancel_notice")
@Data
public class UserCancelNoticeEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;


    /**
     * 用户id
     */
    private String userId;

    /**
     * 取消的消息通知id
     */
    private String cancelNoticeId;


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

}
