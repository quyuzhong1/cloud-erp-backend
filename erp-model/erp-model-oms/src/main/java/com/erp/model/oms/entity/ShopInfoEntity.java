package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

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
@TableName(value = "shop_info", autoResultMap = true)
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
    @TableField(value = "extend_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extendData;

    /**
     * ioss税号
     */
    @TableField("ioss_tax_no")
    private String iossTaxNo;
    /**
     * EORI税号
     */
    @TableField("eori_tax_no")
    private String eoriTaxNo;
    /**
     * 平台店铺状态:none=无, open=正常, closed关闭
     * ShopPlatformStatusEnum
     */
    @TableField("platform_status")
    private String platformStatus;

    /**
     * 类型：overseas 海外, internal 国内
     */
    @TableField("type")
    private String type;
    /**
     * VOEC税号
     */
    @TableField("voec_tax_no")
    private String voecTaxNo;
    
    /**
     * 结算币别
     */
     @TableField("settlement_currency")
     private String settlementCurrency;
     /**
     * 交易币别
     */
     @TableField("trade_currency")
     private String tradeCurrency;
     /**
     * 启用时间
     */
     @TableField("enable_time")
     private LocalDateTime enableTime;
     /**
     * 停用时间
     */
     @TableField("down_time")
     private LocalDateTime downTime;
    /**
     * 数据下载时间
     */
    @TableField("init_pull_time")
    private LocalDateTime initPullTime;
     /**
     * 店铺退货仓库
     */
     @TableField("return_warehouse")
     private String returnWarehouse;
     /**
     * 平台经营模式
     */
     @TableField("business_model")
     private String businessModel;

    /**
     * 授权过期时间
     */
    @TableField(value = "auth_expire_date",fill = FieldFill.INSERT_UPDATE)
    private LocalDate authExpireDate;
    /**
     * token
     */
    @TableField(exist = false)
    private String accessToken;

    /**
     * 时区
     */
    @TableField("time_zone")
    private String timeZone = "";

    public static final String PLATFORM_DICT = "platform_dict";

    public static final String SHOP_CODE = "shop_code";

    public static final String NAME = "name";

    public static final String CUSTOMER_CODE = "customer_code";

    public ShopInfoEntity(String id, Boolean isGenTask) {
        super(id);
        this.isGenTask = isGenTask;
    }

}
