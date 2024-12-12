package com.sdk.wms.antu.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.config.FastJson2LocalDateTimeDeserializer;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AntuCalculateFeeResp extends CleanBaseDTO implements Serializable {

    //总费用
    @JSONField(name = "totalFee")
    private Float totalFee;

    //运输费
    @JSONField(name = "SHIPPING")
    private Float SHIPPING;

    //操作费用
    @JSONField(name = "OPF")
    private Float OPF;

    //燃油附加费
    @JSONField(name = "FSC")
    private Float FSC;

    //关税
    @JSONField(name = "DT")
    private Float DT;

    //挂号
    @JSONField(name = "RSF")
    private Float RSF;

    //仓租
    @JSONField(name = "WHF")
    private Float WHF;

    //其它费用
    @JSONField(name = "OTF")
    private Float OTF;

    //币种
    @JSONField(name = "currency_code")
    private String currencyCode;

    //时效（天）
    @JSONField(name = "invalid")
    private String invalid;

    //派送方式代码
    @JSONField(name = "shipping_method")
    private String shippingMethod;

    //派送方式名称（英文）
    @JSONField(name = "shipping_name")
    private String shippingName;

    //派送方式名称（中文）
    @JSONField(name = "shipping_name_cn")
    private String shippingNameCn;
}
