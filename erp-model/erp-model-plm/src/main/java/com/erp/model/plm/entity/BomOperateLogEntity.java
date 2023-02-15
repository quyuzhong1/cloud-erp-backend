package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * bom 操作记录日志表(BomOperateLog)实体类
 *
 * @author yl
 * @since 2023-01-09 11:32:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_operate_log")
public class BomOperateLogEntity implements Serializable {
    private static final long serialVersionUID = -32922189918551527L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 更新人id
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * bom 表id
     */
    private String bomId;
    /**
     * 操作类型 add 新建  delete 删除 update 编辑  stateUpdate 状态变更 
     */
    private String type;
    /**
     * 变更内容
     */
    private String content;



}

