package com.sdk.oms.temu.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TemuShippingDTO {

    @JSONField(name = "regionName3")
    private String regionName3;
    @JSONField(name = "receiptAdditionalName")
    private Object receiptAdditionalName;
    @JSONField(name = "regionName4")
    private Object regionName4;
    @JSONField(name = "regionName1")
    private String regionName1;
    @JSONField(name = "mail")
    private String mail;
    @JSONField(name = "regionName2")
    private String regionName2;
    @JSONField(name = "mobile")
    private String mobile;
    @JSONField(name = "addressLineAll")
    private String addressLineAll;
    @JSONField(name = "receiptName")
    private String receiptName;
    @JSONField(name = "addressLine1")
    private String addressLine1;
    @JSONField(name = "backupMobile")
    private Object backupMobile;
    @JSONField(name = "postCode")
    private String postCode;
    @JSONField(name = "addressLine2")
    private String addressLine2;
    @JSONField(name = "addressLine3")
    private Object addressLine3;
}
