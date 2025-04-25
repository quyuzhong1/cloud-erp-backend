package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-07 18:28:29
 */
@Data
@TableName("sys_user_info")
public class SysUserInfoEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
   @TableId(value = "uid",type = IdType.ASSIGN_ID)
	private String uid;
	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 真实姓名
	 */
	private String realName;
	/**
	 * 电话号码
	 */
	private String mobile;
	/**
	 * 用户状态1：正常 0：禁用
	 */
	private Integer userState;
	/**
	 * 最近登录的时间
	 */
	private Date lastLoginTime;
	/**
	 * 用户删除状态 1:正常 0：已删除
	 */
	@TableLogic(value = "1",delval = "0")
	private Integer deleteState;
	/**
	 * 最后登录的ip
	 */
	private String lastLoginIp;
	/**
	 * 登录的次数
	 */
	private Integer loginCount;
	/**
	 * 创建时间
	 */
	@TableField(fill= FieldFill.INSERT)
	private LocalDateTime createTime;
	/**
	 * 更新时间
	 */
	@TableField(fill= FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;


	/**
	 * 创建人id
	 */
	@TableField(value = "create_user_id",fill= FieldFill.INSERT)
	private String createUserId;

	/**
	 * 创建人名称
	 */
	@TableField(value = "create_user_name",fill= FieldFill.INSERT)
	private String createUserName;

	/**
	 * 最后修改人id
	 */
	@TableField(value = "update_user_id",fill= FieldFill.INSERT_UPDATE)
	private String updateUserId;

	/**
	 * 最后修改人名称
	 */
	@TableField(value = "update_user_name",fill= FieldFill.INSERT_UPDATE)
	private String updateUserName;

	/**
	 * 账号
	 */
	private String userAccount;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 盐值
	 */
	private String salt;

	/**
	 * 头像图片
	 */
	private String headIcon;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 用户编码
	 */
	@TableField("code")
	private String code;

	/**
	 * 用户类型（默认erp, srm供应商系统）
	 */
	@TableField("user_type")
	private String userType;

	/**
	 * 是否超级管理员 false 不是管理员
	 */
	@TableField("is_super")
	private Boolean isSuper;

	/**
	 * 是否需要修改密码（重置密码首次登录时需要修改密码）
	 */
	@TableField("need_change_pwd")
	private Boolean needChangePwd;
	/**
	 * 同步金蝶id
	 */
	@TableField("sync_kingdee_id")
	private String syncKingdeeId;

	
}
