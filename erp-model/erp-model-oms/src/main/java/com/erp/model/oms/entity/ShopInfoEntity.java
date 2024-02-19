package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * 店铺表
 *
 *
 * @author Lambda
 * @since 2023-06-28
 */
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@TableName("shop_info")
public class ShopInfoEntity extends BaseEntity<ShopInfoEntity> {

    /**
     * 店铺名称
     */
    @TableField("name")
    private String name;

    /**
     * 平台的值
     */
    @TableField("dict_platform")
    private String dictPlatform;



    /**
     * 账号
     */
    @TableField("account")
    private String account;

    /**
     * 销售组织id
     */
    @TableField("sales_org_id")
    private String salesOrgId;

    /**
     * 销售组织名
     */
    @TableField("sales_org_name")
    private String salesOrgName;

    /**
     * 区域id
     */
    @TableField("dict_area_code")
    private String dictAreaCode;


    /**
     * 国家id
     */
    @TableField("dict_country_code")
    private String dictCountryCode;

    /**
     * 国家名
     */
    @TableField("country_name")
    private String countryName;


    /**
     * 负责人id
     */
    @TableField("charge_id")
    private String chargeId;


    /**
     * 负责人id
     */
    @TableField("charge_name")
    private String chargeName;

    /**
     * 禁用状态
     */
    @TableField("disabled")
    private Boolean disabled;


    /**
     * 授权状态
     */
    @TableField("auth_status")
    private String authStatus;

    /**
     * 授权时间
     */
    @TableField("auth_time")
    private LocalDateTime authTime;


    /**
     * 店铺域名
     */
    @TableField("domain")
    private String domain;

    /**
     * 客户的code
     */
    @TableField("customer_code")
    private String customerCode;

    /**
     * 客户的id
     */
    @TableField("customer_id")
    private String customerId;

    /**
     * 是否已生成调度任务
     */
    @TableField("is_gen_task")
    private Boolean isGenTask;

    /**
     * 店铺仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 店铺仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 是否有仓库
     */
    @TableField("is_have_warehouse")
    private Boolean isHaveWarehouse;

    /**
     * 平台店铺编码/卖家编码
     * 亚马逊平台=卖家ID
     */
    @TableField("platform_shop_code")
    private String platformShopCode;

    /**
     * 扩展字段的 数据+值
     */
    @TableField("extend_data")
    private String extendData;

    /**
     * ioss税号
     */
    @TableField("ioss_tax_no")
    private String iossTaxNo;

    public static final String PLATFORM_DICT = "platform_dict";

    public static final String SHOP_CODE = "shop_code";

    public static final String NAME = "name";

    public static final String CUSTOMER_CODE = "customer_code";

    public ShopInfoEntity(String id, Boolean isGenTask) {
        super(id);
        this.isGenTask = isGenTask;
    }

}
