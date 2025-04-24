package com.erp.model.sys.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zdy
 * @ClassName SupplierUserVO
 * @date 2024年01月05日
 * @version: 1.0
 */
@Data
public class SupplierUserVO implements Serializable {
    /**
     * 用户id
     */
    private String uid;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 真实名字
     */
    private String realName;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 权限角色  true 超级管理员 false 业务员
     */
    private Boolean isSuper;
    /**
     * true : 超级管理员
     * false : 业务员
     */
    private String isSuperStr;
    /**
     * 关系id
     */
    private String refId;
    /**
     * 关联供应商ID
     */
    private String supplierId;
    /**
     * 关联供应商
     */
    private String supplierName;
    /**
     *采购员
     */
    private String purchaseUserName;
    /**
     *启用状态  正常 true 禁用 false  //o 禁用 1 正常
     */
    private Integer userState;
    /**
     * 是否禁用 正常 true 禁用 false  //o 禁用 1 正常
     */
    private String userStateStr;
    /**
     * 微信绑定  没有绑定则显示【未绑定】，绑定显示【已绑定】
     */
    private Boolean isBindWechat;
    /**
     * 微信绑定 没有绑定则显示【未绑定】，绑定显示【已绑定】
     */
    private String isBindWechatStr;
    /**
     *最近登录时间
     */
    private Date lastLoginTime;
    private String lastLoginTimeStr;
    /**
     *创建人
     */
    private String createUserName;
    /**
     *创建时间
     */
    private Date createTime;
    private String createTimeStr;
}
