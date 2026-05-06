package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Classname SysDepartmentUserEntity
 * @Date 2022-07-13 18:51
 * @Created by yl
 */
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department_user")
@NoArgsConstructor
@Data
public class SysDepartmentUserEntity extends BaseEntity<SysDepartmentUserEntity> {

    private String departmentId;

    private String userId;

    private Integer leadState;

}
