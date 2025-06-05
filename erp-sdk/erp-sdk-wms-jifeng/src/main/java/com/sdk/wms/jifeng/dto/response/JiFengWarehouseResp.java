package com.sdk.wms.jifeng.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class JiFengWarehouseResp {

    @JSONField(name = "id")
    private Integer id;
    @JSONField(name = "code")
    private String code;
    @JSONField(name = "name")
    private String name;
    @JSONField(name = "type")
    private Integer type;
    @JSONField(name = "selfSending")
    private Object selfSending;
    @JSONField(name = "country")
    private String country;
    @JSONField(name = "province")
    private String province;
    @JSONField(name = "city")
    private String city;
    @JSONField(name = "area")
    private String area;
    @JSONField(name = "address")
    private String address;
    @JSONField(name = "postCode")
    private String postCode;
    @JSONField(name = "timeZone")
    private String timeZone;
    @JSONField(name = "contactPerson")
    private String contactPerson;
    @JSONField(name = "phone")
    private String phone;
    @JSONField(name = "email")
    private String email;
    @JSONField(name = "receiveStatus")
    private Integer receiveStatus;
    @JSONField(name = "orderReceiveStatus")
    private Integer orderReceiveStatus;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "auth")
    private Boolean auth;
}
