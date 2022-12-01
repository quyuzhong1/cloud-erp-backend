package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 店铺表
 * @TableName dmp_shop_info
 */
@TableName(value ="dmp_shop_info")
@Data
public class DmpShopInfo implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id")
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
    @TableField(value = "amazon_site")
    private String amazonSite;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}