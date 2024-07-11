package com.sdk.wangdian.dto;

import com.common.business.dto.CleanBaseDTO;
import lombok.Data;

/**
 * 旺店通店铺数据
 *
 * @author hyj
 * @date 2024-05-24
 */
@Data
public class WdtShopDto extends CleanBaseDTO {
    /**
     * 店铺id, 店铺唯一键
     */
    private Integer shop_id;

    /**
     * 店铺编号
     */
    private String shop_no;

    /**
     * 店铺名称
     */
    private String shop_name;
    /**
     * 平台id
     */
    private String platform_id;
    /**
     * 	子平台id
     */
    private String 	sub_platform_id;

    /**
     * 邮编
     */
    private String zip;

    /**
     * 地址
     */
    private String address;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 手机
     */
    private String mobile;

    /**
     * 备注
     */
    private String remark;

    /**
     * 类别
     */
    private Integer type;

    /**
     * 固话
     */
    private String telno;
    /**
     * Email
     */
    private String email;
    /**
     * 网址
     */
    private String website;

    /**
     * 子类别
     */
    private Integer sub_type;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 修改时间
     */
    private String modified;

    /**
     * 停用
     */
    private Boolean is_disabled;
    /**
     * 分组
     */
    private String group_id;
    /**
     * 平台的主键（平台店铺授权时平台有推送对应值则返回，不推送则返回空字符串）
     */
    private String account_id;

    /**
     * 授权状态: 0：未授权,1：已授权,2：授权失效,3：授权停用
     */
    private Integer auth_state;

    /**
     * 创建时间
     */
    private String created;
    /**
     * 授权时间
     */
    private String auth_time;
}
