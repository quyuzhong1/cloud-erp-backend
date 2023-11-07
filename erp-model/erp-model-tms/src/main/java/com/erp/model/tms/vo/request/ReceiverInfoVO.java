package com.erp.model.tms.vo.request;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Builder;
import lombok.Data;

/**
 * @author zdy
 * @ClassName ReceiverInfoVO
 * @description: 发货人信息
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
@Builder
public class ReceiverInfoVO {
    /**
     * 名称
     */
    @TableField("name")
    private String name;
    /**
     * 公司名
     */
    @TableField("company_name")
    private String companyName;
    /**
     * 联系人
     */
    @TableField("contact")
    private String contact;
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    /**
     * 电话
     */
    @TableField("tel_number")
    private String telNumber;
    /**
     * 国家
     */
    @TableField("country")
    private String country;
    /**
     * 省
     */
    @TableField("province")
    private String province;
    /**
     * 城市
     */
    @TableField("city")
    private String city;
    /**
     * 区
     */
    @TableField("district")
    private String district;
    /**
     * 详细地址1
     */
    @TableField("address_first")
    private String addressFirst;
    /**
     * 详细地址2
     */
    @TableField("address_second")
    private String addressSecond;
    /**
     * 邮编
     */
    @TableField("zip_code")
    private String zipCode;
}
