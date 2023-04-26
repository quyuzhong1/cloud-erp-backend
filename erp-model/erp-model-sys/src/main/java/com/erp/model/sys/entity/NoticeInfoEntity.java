package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 通知信息表
 * @author lambda
 * @since 2023-04-20
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("notice_info")
public class NoticeInfoEntity extends BaseEntity<NoticeInfoEntity> {

    /**
     * 禁用状态 false 没有  true禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 通知节点id
     */
    @TableField("node_id")
    private String nodeId;


    /**
     * 通知系统
     */
    @TableField("system")
    private String system;


    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;




}
