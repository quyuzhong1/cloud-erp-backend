package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
public class ShopDTO {

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
    private String amazonSite;

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
    private Date createTime;

    /**
     * 平台标识
     */
    private String platformSign;

    /**
     * 部门id
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 负责人名称
     */
    private String chargeName;


}
