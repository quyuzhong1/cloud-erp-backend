package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;


/**
 * 订单DTO 所有平台(B2C销售订单买家信息)通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 * @since 2023-10-09
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformOrderReceiverDTO implements Serializable {
    /**
     * 买家登录id
     */
    private String loginId;
    /**
     * 买家id
     */
    private String customerId;
    /**
     * 买家全名
     */
    private String name;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 买家电话
     */
    private String telNumber;
    /**
     * 收货地址1
     */
    private String firstAddress;
    /**
     * 收货地址2
     */
    private String secondAddress;
    /**
     * 城市名称
     */
    private String cityName;
    /**
     * 国家名称
     */
    private String countryName;
    /**
     * 收货人名称
     */
    private String receiverName;
    /**
     * 收货人电话
     */
    private String receiverTelNumber;
    /**
     * 邮编
     */
    private String postCode;
    /**
     * 街道详细地址
     */
    private String fullAddress;
    /**
     * 国家二字码
     */
    private String country;
    /**
     * 省
     */
    private String provinceName;
    /**
     * 区
     */
    private String districtName;

    /**
     * 收件人税号
     */
    private String receiverTaxNo;

    /**
     * 是否更新订单异常
     */
    private Boolean isUpdateError;
    /**
     * IE号
     */
    private String ieNo;

    public String getCountry() {
        // 全局替换UK为GB
        if ("UK".equalsIgnoreCase(this.country)){
            return "GB";
        }
        return country;
    }
}