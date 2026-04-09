package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
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

    private String postId;

    private String userId;

}
