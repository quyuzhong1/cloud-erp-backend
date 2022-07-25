package com.cloud.erp.admin.modules.sys.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname SysUserManageVO
 * @Description TODO
 * @Date 2022-07-22 16:46
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserManageVO  implements Serializable {

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

    private Date createTime;
    /**
     * 更新时间
     */

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

    private String mail;

    private List<String> roleIdList;
}
