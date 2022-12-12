package com.erp.server.dmp.entity.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.*;

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

    @Panno(findType = PannoEnum.EQ,field = "id")
    private String id;

    @Panno(findType = PannoEnum.EQ,field = "stockSku")
    private String stockSku;

    @Panno(findType = PannoEnum.EQ,field = "platformCode")
    private String platformCode;

}
