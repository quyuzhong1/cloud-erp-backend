package com.sdk.wms.iml.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImlQueryOutboundResp extends CleanBaseDTO {

    @JSONField(name = "orderNo")
    private String orderNo;
    @JSONField(name = "bizType")
    private String bizType;
    @JSONField(name = "ecPlatform")
    private String ecPlatform;
    @JSONField(name = "ecPlatformOrderNo")
    private String ecPlatformOrderNo;
    @JSONField(name = "status")
    private String status;
    @JSONField(name = "orderStatus")
    private String orderStatus;
    @JSONField(name = "orderSubStatus")
    private String orderSubStatus;
    @JSONField(name = "platformOrderNo")
    private String platformOrderNo;
    @JSONField(name = "ecPlatForm")
    private String ecPlatForm;
    @JSONField(name = "warehouseCode")
    private String warehouseCode;
    @JSONField(name = "logisticsCode")
    private String logisticsCode;
    @JSONField(name = "trackNumber")
    private String trackNumber;
    @JSONField(name = "buyerCountry")
    private String buyerCountry;
    @JSONField(name = "buyerProvince")
    private String buyerProvince;
    @JSONField(name = "buyerCity")
    private String buyerCity;
    @JSONField(name = "buyerAddress")
    private String buyerAddress;
    @JSONField(name = "buyerAddress2")
    private String buyerAddress2;
    @JSONField(name = "buyerName")
    private String buyerName;
    @JSONField(name = "buyerPhone")
    private String buyerPhone;
    @JSONField(name = "buyerEmail")
    private String buyerEmail;
    @JSONField(name = "buyerPostcode")
    private String buyerPostcode;
    @JSONField(name = "buyerHouseNumber")
    private String buyerHouseNumber;
    @JSONField(name = "buyerCompanyName")
    private String buyerCompanyName;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "detailList")
    private List<DetailListDTO> detailList;
    @JSONField(name = "ullageFlag")
    private String ullageFlag;
    @JSONField(name = "insuranceService")
    private String insuranceService;
    @JSONField(name = "valueAddedServiceCodes")
    private String valueAddedServiceCodes;
    @JSONField(name = "skuOutStockBatch")
    private String skuOutStockBatch;
    @JSONField(name = "totalWeight")
    private Integer totalWeight;
    @JSONField(name = "totalVolume")
    private Integer totalVolume;
    @JSONField(name = "exceptionMsg")
    private String exceptionMsg;
    @JSONField(name = "outTime")
    private Integer outTime;

    @NoArgsConstructor
    @Data
    public static class DetailListDTO {
        @JSONField(name = "skuBarcode")
        private String skuBarcode;
        @JSONField(name = "declaredAmount")
        private Integer declaredAmount;
        @JSONField(name = "skuCount")
        private Integer skuCount;
        @JSONField(name = "skuBatchInfo")
        private String skuBatchInfo;
        @JSONField(name = "actualCount")
        private Integer actualCount;
    }
}
