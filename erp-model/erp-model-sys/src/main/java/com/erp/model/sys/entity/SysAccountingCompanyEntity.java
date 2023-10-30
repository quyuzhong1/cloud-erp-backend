package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Administrator
 * @Classname ysAccountingCompanyEntity

 * @Date 2022-07-12 9:39
 * @Created by yl
 */
@Data
@TableName("sys_accounting_company")
public class SysAccountingCompanyEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 公司编码
     */
    private String code;

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

    /**
     * 联系电话
     */
    private String contactMobile;

    /**
     * 联系人地址
     */
    private String contactAddress;

    /**
     * 币种
     */
    private String currency;

    /**
     * 金蝶的id
     */
    private String kingdeeId;

    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createUserId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
}
