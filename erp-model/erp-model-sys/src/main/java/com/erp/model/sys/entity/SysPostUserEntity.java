package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * @Classname SysPostUserEntity

 * @Date 2022-07-12 9:55
 * @Created by yl
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_post_user")
public class SysPostUserEntity extends BaseEntity<SysPostUserEntity> {

    @TableId(value = "id",type = IdType.ASSIGN_ID )
    private String id;


    private String postId;

    private String userId;

}
