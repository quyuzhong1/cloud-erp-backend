package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 第三方系统店铺表
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_shop")
public class ThirdShopEntity extends BaseEntity<ThirdShopEntity> {

    /**
     * 是否禁用/停用 true 是 false 不是
     */
    @TableField("disabled")
    private Boolean disabled;
    /**
     * 系统类型：lingxing领星，wangdian旺店通
     */
    @TableField("sys_type")
    private String sysType;
    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 平台id
     */
    @TableField("platform_id")
    private String platformId;
    /**
     * 子平台id
     */
    @TableField("sub_platform_id")
    private String subPlatformId;
    /**
     * 分组id
     */
    @TableField("group_id")
    private String groupId;
    /**
     * 平台的主键（平台店铺授权时平台有推送对应值则返回，不推送则返回空字符串）
     */
    @TableField("account_id")
    private String accountId;
    /**
     * 授权状态：0未授权 1已授权 2授权失效 3授权停用
     */
    @TableField("auth_state")
    private Integer authState;
    /**
     * 授权时间
     */
    @TableField("auth_time")
    private String authTime;
    /**
     * 编号
     */
    @TableField("code")
    private String code;
    /**
     * 名称
     */
    @TableField("name")
    private String name;
    /**
     * 地址
     */
    @TableField("address")
    private String address;
    /**
     * 联系人
     */
    @TableField("contacts")
    private String contacts;
    /**
     * 联系人电话
     */
    @TableField("tel_number")
    private String telNumber;
    /**
     * 固话
     */
    @TableField("telno")
    private String telno;
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    /**
     * 邮编
     */
    @TableField("zip")
    private String zip;
    /**
     * 网址
     */
    @TableField("website")
    private String website;
    /**
     * 省份
     */
    @TableField("province")
    private String province;
    /**
     * 城市
     */
    @TableField("city")
    private String city;
    /**
     * 区县
     */
    @TableField("district")
    private String district;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 第三方创建时间
     */
    @TableField("created")
    private String created;
    /**
     * 第三方修改时间
     */
    @TableField("modified")
    private String modified;


    public static final String DISABLED = "disabled";

    public static final String SYS_TYPE = "sys_type";

    public static final String SHOP_ID = "shop_id";

    public static final String PLATFORM_ID = "platform_id";

    public static final String SUB_PLATFORM_ID = "sub_platform_id";

    public static final String GROUP_ID = "group_id";

    public static final String ACCOUNT_ID = "account_id";

    public static final String AUTH_STATE = "auth_state";

    public static final String AUTH_TIME = "auth_time";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String ADDRESS = "address";

    public static final String CONTACTS = "contacts";

    public static final String TEL_NUMBER = "tel_number";

    public static final String TELNO = "telno";

    public static final String EMAIL = "email";

    public static final String ZIP = "zip";

    public static final String WEBSITE = "website";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DISTRICT = "district";

    public static final String REMARK = "remark";

    public static final String CREATED = "created";

    public static final String MODIFIED = "modified";

    @Override
    public Serializable pkVal() {
        return null;
    }

}