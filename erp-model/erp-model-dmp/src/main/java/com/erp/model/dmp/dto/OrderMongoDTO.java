package com.erp.model.dmp.dto;

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
    private String refundplatformOrderId;

}
