package com.sdk.wangdian.dto;

import com.common.business.dto.UniqueDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @date 2024-06-14
 * @author hyj
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ErpVirtualWarehouseDto extends UniqueDto {
    /**
     * 是否禁用/停用 true 是 false 不是
     */
    private Boolean disabled;
    /**
     * 系统类型：lingxing领星，wangdian旺店通
     */
    private String sysType;
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 类型
     */
    private String type;
    /**
     * 子类型
     */
    private String subType;
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
     * 第三方创建时间
     */
    private String created;
    /**
     * 第三方修改时间
     */
    private String modified;
    /**
     * 备注
     */
    private String remark;
}
