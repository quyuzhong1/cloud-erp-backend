package com.erp.tms.aliexpress.model.handover;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName AddressBase
 * @description: TODO
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressBase implements Serializable {
    /**
     *邮编
     */
    @JSONField(name = "zip_code")
    private String zipCode;
    /**
     *详细地址
     */
    @JSONField(name = "detail_address")
    private String detailAddress;
    /**
     *街道
     */
    @JSONField(name = "street")
    private String street;
    /**
     *区
     */
    @JSONField(name = "district")
    private String district;
    /**
     *城市
     */
    @JSONField(name = "city")
    private String city;
    /**
     *省份
     */
    @JSONField(name = "province")
    private String province;

    /**
     *国家
     */
    @JSONField(name = "country")
    private String country;
}
