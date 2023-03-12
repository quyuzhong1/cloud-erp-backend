package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 变更管理变更实体的信息表(ProductChangeDetails)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_change_details")
public class ProductChangeDetailsEntity implements Serializable {
    private static final long serialVersionUID = -76172530084005528L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 修改实体的json
     */
    private String detailsJson;
    /**
     * 变更表id
     */
    private String changeInfoId;



}

