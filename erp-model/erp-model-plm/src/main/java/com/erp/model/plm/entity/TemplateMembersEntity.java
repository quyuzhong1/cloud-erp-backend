package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName template_members 模板成员表
 */
@Data
@TableName(value ="template_members")
public class TemplateMembersEntity extends BaseEntity implements Serializable {

    /**
     * 成员id
     */
    private String memberId;

    /**
     * 成员名称
     */
    private String memberName;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 是否是负责人 0 不是 1 是
     */
    private Short isCharge;

    private static final long serialVersionUID = 1L;
}