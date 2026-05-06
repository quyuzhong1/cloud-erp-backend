package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Classname SysPostEntity
 * @Date 2022-07-12 9:55
 * @Created by yl
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_post")
public class SysPostEntity extends BaseEntity<SysPostEntity> {

    private String postName;

    private String postRemark;

}
