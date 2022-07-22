package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ysAccountingCompanyEntity
 * @Description TODO
 * @Date 2022-07-12 9:39
 * @Created by yl
 */
@Data
@TableName("sys_accounting_company")
public class SysAccountingCompanyEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 公司名
     */
    private String companyName;

    /**
     * 公司地址
     */
    private String companyAddress;

    /**
     * 联系人名字
     */
    private String contactName;

    private String contactMobile;

    /**
     * 联系人地址
     */
    private String contactAddress;

    private String currency;

    //状态 1 正常 0 禁用
    private Integer companyState;

    @TableLogic(value = "1",delval = "0")
    private Integer deleteState;
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
}
