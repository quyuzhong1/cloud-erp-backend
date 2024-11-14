package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("customer_info")
public class CustomerInfoEntity extends BaseEntity<CustomerInfoEntity> {

    /**
     * code
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;


    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 分组id
     */
    @TableField("group_id")
    private String groupId;

    /**
     * 分组名
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 国家id
     */
    @TableField("country_id")
    private String countryId;

    /**
     * 地区id
     */
    @TableField("area_id")
    private String areaId;

    /**
     * 省id
     */
    @TableField("province_id")
    private String provinceId;

    /**
     * 城市id
     */
    @TableField("city_id")
    private String cityId;

    /**
     * 客户名称
     */
    @TableField("name")
    private String name;

    /**
     * 使用组织id
     */
    @TableField("use_org_id")
    private String useOrgId;

    @TableField("use_org_name")
    private String useOrgName;


    /**
     * 内部组织id
     */
    @TableField("inner_org_id")
    private String innerOrgId;

    /**
     * 内部组织id
     */
    @TableField("inner_org_name")
    private String innerOrgName;

    /**
     * 简称
     */
    @TableField("short_name")
    private String shortName;

    /**
     * 平台类型
     */
    @TableField("platform_type")
    private String platformType;

    /**
     * 公司分类 从 oms_dict 获取
     */
    @TableField("company_category_dict")
    private String companyCategoryDict;

    /**
     * 是否禁用 true 禁用 
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 付款方
     */
    @TableField("pay_code")
    private String payCode;

    /**
     * 结算方
     */
    @TableField("settle_code")
    private String settleCode;

    /**
     * 结算方式   oms_dict 获取
     */
    @TableField("settle_dict")
    private String settleDict;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 占用状态
     */
    @TableField("occupy_status")
    private Boolean occupyStatus;


    /**
     * 销售员id
     */
    @TableField("seller_id")
    private String sellerId;

    /**
     * 销售员
     */
    @TableField("seller_name")
    private String sellerName;

    /**
     * 条件字典 oms_dict 获取
     */
    @TableField("condition_dict")
    private String conditionDict;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 客户属性
     */
    @TableField("customer_property")
    private String customerProperty;

    /**
     * 通讯地址
     */
    @TableField("mail_address")
    private String mailAddress;
    
    /**
     * 结算币别
     */
     @TableField("settlement_currency")
     private String settlementCurrency;
     /**
     * 财务组织
     */
     @TableField("financial_organization")
     private String financialOrganization;
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
     * 交易币别
     */
     @TableField("trade_currency")
     private String tradeCurrency;
     /**
     * 平台类型:o2b=线上2B，o2c=线上2C，x2b=线下2B，x2c=线上2C，o2o=O2O  枚举：CustomerInfoBusinessModeEnum
     */
     @TableField("business_mode")
     private String businessMode;
     /**
     * 交易模式:dbjy=担保交易，xkhh=先款后货  枚举：CustomerInfoTransactionalModeEnum
     */
     @TableField("transactional_mode")
     private String transactionalMode;
     /**
     * 账期设置：month=自然月,platform=平台自定义账期,shop=店铺自定义  枚举：CustomerInfoPeriodSettingEnum
     */
     @TableField("period_setting")
     private String periodSetting;
     /**
     * 确收方式:ship=签收确收，settlement=结算确收，default=自定义确收方式  枚举：CustomerInfoCheckTypeEnum
     */
     @TableField("check_type")
     private String checkType;
     /**
     * 区域id 对应sys
     */
     @TableField("dict_area_code")
     private String dictAreaCode;
     /**
     * 国家id
     */
     @TableField("dict_country_code")
     private String dictCountryCode;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String GROUP_ID = "group_id";

    public static final String COUNTRY_ID = "country_id";

    public static final String AREA_ID = "area_id";

    public static final String PROVINCE_ID = "province_id";

    public static final String CITY_ID = "city_id";

    public static final String NAME = "name";

    public static final String USE_ORG_ID = "use_org_id";

    public static final String INNER_ORG_ID = "inner_org_id";

    public static final String SHORT_NAME = "short_name";

    public static final String PLATFORM_DICT = "platform_dict";

    public static final String COMPANY_CATEGORY_DICT = "company_category_dict";

    public static final String DISABLED = "disabled";

    public static final String PAY_NAME = "pay_name";

    public static final String SETTLE_NAME = "settle_name";

    public static final String SETTLE_DICT = "settle_dict";

    public static final String CURRENCY = "currency";

    public static final String REMARK = "remark";

    public static final String CONDITION_DICT = "condition_dict";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
