package com.sdk.wangdian.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.UniqueDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author hyj
 * @date 2024-05-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ErpShopDto extends UniqueDto {
    /**
     * 是否禁用/停用 true 是 false 不是
     */
    private Boolean disabled;
    /**
     * 系统类型：lingxing领星，wangdian旺店通
     */
    private String sysType;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 平台id
     */
    private String platformId;
    /**
     * 子平台id
     */
    private String subPlatformId;
    /**
     * 分组id
     */
    private String groupId;
    /**
     * 平台的主键（平台店铺授权时平台有推送对应值则返回，不推送则返回空字符串）
     */
    private String accountId;
    /**
     * 授权状态：0未授权 1已授权 2授权失效 3授权停用
     */
    private Integer authState;
    /**
     * 授权时间
     */
    private String authTime;
    /**
     * 编号
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 地址
     */
    private String address;
    /**
     * 联系人
     */
    private String contacts;
    /**
     * 联系人电话
     */
    private String telNumber;
    /**
     * 固话
     */
    private String telno;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 邮编
     */
    private String zip;
    /**
     * 网址
     */
    private String website;
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
     * 备注
     */
    private String remark;
    /**
     * 第三方创建时间
     */
    private String created;
    /**
     * 第三方修改时间
     */
    private String modified;

}
