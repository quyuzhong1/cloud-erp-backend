package com.erp.model.dmp.dto;

import cn.hutool.core.annotation.Alias;
import cn.hutool.core.util.StrUtil;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  OrderMongoDTO {
    @Panno(findType = PannoEnum.EQ,field = "fBillNo")
    private String billNo;

    @Panno(findType = PannoEnum.EQ,field = "fOrderNo")
    private String orderNo;

    @Panno(findType = PannoEnum.EQ,field = "platformOrderId")
    private String platformOrderId;

    @Panno(findType = PannoEnum.EQ,field = "_id")
    private String id;

    @Panno(findType = PannoEnum.EQ,field = "stockSku")
    private String stockSku;

    @Panno(findType = PannoEnum.EQ,field = "platformCode")
    private String platformCode;

    @Panno(findType = PannoEnum.EQ,field = "code")
    private String code;

    @Panno(findType = PannoEnum.EQ,field = "salesRecordNumber")
    private String salesRecordNumber;
    @Panno(findType = PannoEnum.EQ,field = "refundplatformOrderId")
    private String refundPlatformOrderId;

    @Panno(findType = PannoEnum.EQ,field = "fId")
    private String fId;

    @Panno(findType = PannoEnum.EQ,field = "fMaterialId")
    private String fMaterialId;

    @Panno(findType = PannoEnum.EQ,field = "downloadStatus")
    private Integer downloadStatus;

    @Panno(findType = PannoEnum.EQ,field = "isClean")
    private Integer isClean;

    @Panno(findType = PannoEnum.EQ,field = "cleanToDelivery")
    private Integer cleanToDelivery;

    @Panno(findType = PannoEnum.EXISTS,field = "cleanToDelivery")
    private Integer cleanToDeliveryExists;

    @Panno(findType = PannoEnum.LTE,field = "downloadTime")
    private LocalDateTime downloadEndTime;
    @Panno(findType = PannoEnum.EQ, field = "comboSku")
    private String comboSku;

    @Panno(findType=PannoEnum.EQ,  field = "returnOrderId")
    private String returnOrderId;

    @Panno(findType=PannoEnum.EQ,  field = "fNumber")
    private String fNumber;
    public static OrderMongoDTO getByFBillNo(String fBillNo) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setBillNo(fBillNo);
        return orderMongoDTO;
    }
    public OrderMongoDTO(String platformCode, String code) {
        this.platformCode = platformCode;
        this.code = code;
    }

    public static OrderMongoDTO getByCode(String code) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setCode(code);
        return orderMongoDTO;
    }

    public OrderMongoDTO(String id) {
        this.id = id;
    }

    public static OrderMongoDTO getByFIdAndBillNo(String fBillNo, String fId) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setBillNo(fBillNo);
        orderMongoDTO.setFId(fId);
        return orderMongoDTO;
    }

    public static OrderMongoDTO getByBillNoAndOrderNo(String fBillNo, String fOrderNo) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setBillNo(fBillNo);
        if(StrUtil.isNotBlank(fOrderNo)){
            orderMongoDTO.setOrderNo(fOrderNo);
        }
        return orderMongoDTO;
    }

    public static OrderMongoDTO getByOrderIdAndSaleNum(String platformOrderId, String salesRecordNumber) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setPlatformOrderId(platformOrderId);
        orderMongoDTO.setSalesRecordNumber(salesRecordNumber);
        return orderMongoDTO;
    }
    public static OrderMongoDTO getByPlatformOrderId(String platformOrderId) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setPlatformOrderId(platformOrderId);
        return orderMongoDTO;
    }

    public static OrderMongoDTO getShopByMaterialId(String fMaterialId) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setFMaterialId(fMaterialId);
        return orderMongoDTO;
    }

    public static OrderMongoDTO getByDownloadStatus(Integer status) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setDownloadStatus(status);
        return orderMongoDTO;
    }

    public static OrderMongoDTO getByIsClean(Integer isClean, Integer diffMinute) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setIsClean(isClean);
        if(null != diffMinute && diffMinute != 0){
            orderMongoDTO.setDownloadEndTime(LocalDateTime.now().minusMinutes(diffMinute));
        }
        return orderMongoDTO;
    }

    public static OrderMongoDTO getByCleanToDelivery(Integer isClean, Integer cleanToDelivery) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        if(null != isClean){
            orderMongoDTO.setCleanToDelivery(isClean);
        }
        if (null != cleanToDelivery){
            orderMongoDTO.setCleanToDeliveryExists(cleanToDelivery);
        }
        return orderMongoDTO;
    }

    public static OrderMongoDTO getByComboSku(String comboSku) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setComboSku(comboSku);
        return orderMongoDTO;
    }

    public static OrderMongoDTO getByReturnOrderId(String returnCode) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setReturnOrderId(returnCode);
        return orderMongoDTO;
    }
    public static OrderMongoDTO getByFNumber(String fNumber) {
        OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
        orderMongoDTO.setFNumber(fNumber);
        return orderMongoDTO;
    }
}
