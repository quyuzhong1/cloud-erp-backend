package com.sdk.wms.iml.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImlWarehouseResp implements Serializable {

    //仓库代码
    @JSONField(name = "warehouse_code")
    private String warehouse_code;

    //仓库名称
    @JSONField(name = "warehouse_name")
    private String warehouse_name;

    //仓库所在国家二字码
    @JSONField(name = "country_code")
    private String country_code;

    //仓库类型
    @JSONField(name = "warehouse_type")
    private String warehouse_type;

    //仓库所在国家ID
    @JSONField(name = "country_id")
    private String country_id;

    //邮编
    @JSONField(name = "postcode")
    private String postcode;

    //省
    @JSONField(name = "state")
    private String state;

    //城市
    @JSONField(name = "city")
    private String city;

    //地址
    @JSONField(name = "street_address1")
    private String street_address1;

    //地址2
    @JSONField(name = "street_address2")
    private String street_address2;

    //编号
    @JSONField(name = "street_number")
    private String street_number;
}
