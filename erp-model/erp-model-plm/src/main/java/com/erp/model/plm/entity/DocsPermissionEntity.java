package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName docs_permission
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("docs_permission")
public class DocsPermissionEntity implements Serializable {


    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;



    /**
     * 交付文档id
     */
    private String deliveryDocsId;

    /**
     * 查看的用户id
     */
    private String queryUserId;

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