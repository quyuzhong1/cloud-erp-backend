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
 * @date 2022-07-11 14:05:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user_third")
public class SysUserThirdEntity extends BaseEntity<SysUserThirdEntity> {

	/**
	 * $column.comments
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 第三方平台类型
	 */
	private String thirdPartyType;
	/**
	 * 第三方用户对应的唯一id
	 */
	private String thirdUnionId;
	/**
	 * 第三方用户对应的open_id
	 */
	private String thirdOpenId;
	/**
	 * 第三方用户对应的user_id
	 */
	private String thirdUserId;

}
