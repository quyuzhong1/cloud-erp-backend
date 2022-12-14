package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:48
 */
@Data
@NoArgsConstructor
public class DmpShopInfoDTO {
    /**
     * 主键id
     */
    private String id;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 店铺站点
     */
    private String site;

    /**
     * 负责人名称
     */
    private String chargeName;

    /**
     * 店铺状态
     */
    private Integer status;

    /**
     * 店铺标识
     */
    private String storeSign;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 更新人
     */
    private String updateUserName;
}
