package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-08 11:23:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role_user")
public class SysRoleUserEntity extends BaseEntity<SysRoleUserEntity> {

	/**
	 * 角色id
	 */
	private String roleId;
	/**
	 * 用户id
	 */
	private String userId;

}
