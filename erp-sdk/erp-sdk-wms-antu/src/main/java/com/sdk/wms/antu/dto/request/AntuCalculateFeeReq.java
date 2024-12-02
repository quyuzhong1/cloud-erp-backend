package com.sdk.wms.antu.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AntuCalculateFeeReq extends AntuBaseRequest {

    //	发货仓库代码
    @JSONField(name = "warehouse_code")
    private String warehouseCode;

    //目的国家代码
    @JSONField(name = "country_code")
    private String countryCode;

    //配送方式
    @JSONField(name = "shipping_method")
    private List<String> shippingMethod;

    //邮政编码
    @JSONField(name = "postcode")
    private String postcode;

    //包裹重量
    @JSONField(name = "weight")
    private Float weight;

    //包裹长
    @JSONField(name = "length")
    private Float length;

    //包裹宽
    @JSONField(name = "width")
    private Float width;

    //包裹高
    @JSONField(name = "height")
    private Float height;

    //地址1
    @JSONField(name = "address1")
    private String address1;

    //省
    @JSONField(name = "state")
    private String state;
}
