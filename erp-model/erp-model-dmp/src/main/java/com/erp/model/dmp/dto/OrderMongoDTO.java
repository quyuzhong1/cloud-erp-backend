package com.erp.model.dmp.dto;

import cn.hutool.core.util.StrUtil;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
