package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName docs_permission
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("docs_permission")
public class DocsPermissionEntity extends BaseEntity<DocsPermissionEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交付文档id
     */
    private String deliveryDocsId;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 产品id
     */
    private String taskId;

    /**
     * 查看的用户id
     */
    private String queryRoleId;

}