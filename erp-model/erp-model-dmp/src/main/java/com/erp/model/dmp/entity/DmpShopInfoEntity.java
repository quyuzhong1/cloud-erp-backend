package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 店铺表
 * @TableName dmp_shop_info
 */
@TableName(value ="dmp_shop_info")
@Data
public class DmpShopInfoEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 平台店铺编号
     */
    @TableField(value = "plarform_shop_no")
    private String plarformShopNo;

    /**
     * 平台店铺账户
     */
    @TableField(value = "account_user_name")
    private String accountUserName;

    /**
     * 平台店铺标识
     */
    @TableField(value = "account_store_name")
    private String accountStoreName;

    /**
     * 店铺名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 店铺站点
     */
    @TableField(value = "site")
    private String site;

    /**
     * 店铺状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 平台名称
     */
    @TableField(value = "platform_name")
    private String platformName;

    /**
     * 财务编码
     */
    @TableField(value = "finance_code")
    private String financeCode;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

    /**
     * 平台标识
     */
    @TableField(value = "platform_sign")
    private String platformSign;

    /**
     * 负责人id
     */
    @TableField(value = "charge_id")
    private String chargeId;

    /**
     * 负责人名称
     */
    @TableField(value = "charge_name")
    private String chargeName;

    /**
     * 店铺标识
     */
    @TableField(value = "store_sign")
    private String storeSign;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpShopInfoEntity{" +
                "plarformShopNo='" + plarformShopNo + '\'' +
                ", accountUserName='" + accountUserName + '\'' +
                ", accountStoreName='" + accountStoreName + '\'' +
                ", name='" + name + '\'' +
                ", site='" + site + '\'' +
                ", status=" + status +
                ", platformName='" + platformName + '\'' +
                ", financeCode='" + financeCode + '\'' +
                ", platformSign='" + platformSign + '\'' +
                '}';
    }
}