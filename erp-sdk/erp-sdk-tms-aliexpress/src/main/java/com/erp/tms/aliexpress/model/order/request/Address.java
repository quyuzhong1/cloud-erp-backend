package com.erp.tms.aliexpress.model.order.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Address
 * @description: TODO
 * @date 2023年11月17日
 * @version: 1.0
 */
@Data
public class Address implements Serializable {
    /**
     *电话
     */
    @JSONField(name = "phone")
    private String phone;
    /**
     *传真
     */
    @JSONField(name = "fax")
    private String fax;
    /**
     *是否需要更新
     */
    @JSONField(name = "need_to_update")
    private String needToUpdate;
    /**
     *是否需要更新
     */
    @JSONField(name = "is_default")
    private int isDefault;
    /**
     *类型
     */
    @JSONField(name = "member_type")
    private String memberType;
    /**
     *旺旺
     */
    @JSONField(name = "trademanage_id")
    private String tradeManageId;
    /**
     *邮编
     */
    @JSONField(name = "post_code")
    private String postCode;
    /**
     *街道
     */
    @JSONField(name = "street")
    private String street;
    /**
     *国家
     */
    @JSONField(name = "country")
    private String country;
    /**
     *城市
     */
    @JSONField(name = "city")
    private String city;
    /**
     *区
     */
    @JSONField(name = "county")
    private String county;
    /**
     *邮箱
     */
    @JSONField(name = "email")
    private String email;
    /**
     *卖家后台地址id,用来获取卖家详细地址信息，传入值为Long型；传入addressId后，其余字段值无效。
     */
    @JSONField(name = "address_id")
    private String addressId;
    /**
     *
     * 姓名
     */
    @JSONField(name = "name")
    private String name;
    /**
     *省份
     */
    @JSONField(name = "province")
    private String province;
    /**
     *详细地址
     */
    @JSONField(name = "street_address")
    private String streetAddress;
    /**
     *电话
     */
    @JSONField(name = "mobile")
    private String mobile;
    /**
     *语言 默认CN
     */
    @TableField("language")
    private String language;
}
