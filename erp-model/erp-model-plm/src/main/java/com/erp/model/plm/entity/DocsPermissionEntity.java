package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;








}