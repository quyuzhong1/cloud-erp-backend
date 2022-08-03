package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
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
	 * $column.comments
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
	private Date createTime;
	/**
	 * 更新时间
	 */
	@TableField(fill= FieldFill.INSERT_UPDATE)
	private Date updateTime;
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

	private String headIcon;


	private String email;




}
