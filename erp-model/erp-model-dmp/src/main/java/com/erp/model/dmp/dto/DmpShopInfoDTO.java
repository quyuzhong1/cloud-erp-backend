package com.erp.model.dmp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 店铺表
 * @TableName dmp_shop_info
 */
@Data
public class DmpShopInfoDTO implements Serializable {
    /**
     * 主键id
     */
    private String id;

    /**
     * 平台店铺编号
     */
    private String plarformShopNo;

    /**
     * 平台店铺账户
     */
    private String accountUserName;

    /**
     * 平台店铺标识
     */
    private String accountStoreName;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 店铺站点
     */
    private String site;

    /**
     * 店铺状态
     */
    private Integer status;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 财务编码
     */
    private String financeCode;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 平台标识
     */
    private String platformSign;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 负责人名称
     */
    private String chargeName;

    /**
     * 部门id
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 店铺标识
     */
    private String storeSign;

    /**
     * 启用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enableTime;


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