package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
    private String approveStatus;

    /**
     * 分组id
     */
    @TableField("group_id")
    private String groupId;

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

    /**
     * 内部组织id
     */
    @TableField("inner_org_id")
    private String innerOrgId;

    /**
     * 简称
     */
    @TableField("short_name")
    private String shortName;

    /**
     * 平台类型 从sys_dict 获取
     */
    @TableField("platform_dict")
    private String platformDict;

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
    @TableField("pay_name")
    private String payName;

    /**
     * 结算方
     */
    @TableField("settle_name")
    private String settleName;

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
     * 条件字典 oms_dict 获取
     */
    @TableField("condition_dict")
    private String conditionDict;


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
