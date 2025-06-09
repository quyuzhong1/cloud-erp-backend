package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname UserCancelNoticeEntity

 * @Date 2022-11-10 15:01
 * @Created by yl
 */
@TableName(value = "user_cancel_notice")
@Data
public class UserCancelNoticeEntity extends BaseEntity<UserCancelNoticeEntity> implements Serializable {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 取消的消息通知id
     */
    private String cancelNoticeId;

}
