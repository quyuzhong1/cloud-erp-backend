package com.sdk.wms.weishi.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiCreateOutboundRequest {

    @JSONField(name = "warehouse")
    private String warehouse;
    @JSONField(name = "erpNo")
    private String erpNo;
    @JSONField(name = "platform")
    private String platform;
    @JSONField(name = "platformOrderNo")
    private String platformOrderNo;
    @JSONField(name = "buyerName")
    private String buyerName;
    @JSONField(name = "buyerPhone")
    private String buyerPhone;
    @JSONField(name = "recipientCountry")
    private String recipientCountry;
    @JSONField(name = "recipientProvince")
    private String recipientProvince;
    @JSONField(name = "recipientCity")
    private String recipientCity;
    @JSONField(name = "recipientArea")
    private String recipientArea;
    @JSONField(name = "recipientAddress")
    private String recipientAddress;
    @JSONField(name = "recipientAddress2")
    private String recipientAddress2;
    @JSONField(name = "recipientEmail")
    private String recipientEmail;
    @JSONField(name = "zipCode")
    private String zipCode;
    @JSONField(name = "taxId")
    private String taxId;
    @JSONField(name = "recipientCompany")
    private String recipientCompany;
    @JSONField(name = "type")
    private Integer type;
    @JSONField(name = "logisticsId")
    private Integer logisticsId;
    @JSONField(name = "logisticsName")
    private String logisticsName;
    @JSONField(name = "amount")
    private Integer amount;
    @JSONField(name = "currency")
    private String currency;
    @JSONField(name = "trackingNo")
    private String trackingNo;
    @JSONField(name = "labelUrl")
    private String labelUrl;
    @JSONField(name = "expireTime")
    private String expireTime;
    @JSONField(name = "skuList")
    private List<SkuListDTO> skuList;
    @JSONField(name = "packageType")
    private Integer packageType;
    @JSONField(name = "pickingNote")
    private String pickingNote;

    @NoArgsConstructor
    @Data
    public static class SkuListDTO {
        @JSONField(name = "skuId")
        private Integer skuId;
        @JSONField(name = "sku")
        private String sku;
        @JSONField(name = "num")
        private Integer num;
        @JSONField(name = "unitPrice")
        private Integer unitPrice;
        @JSONField(name = "itemNameEn")
        private String itemNameEn;
        @JSONField(name = "itemNameCn")
        private String itemNameCn;
        @JSONField(name = "hsCode")
        private String hsCode;
    }
}
