package com.erp.server.dmp.entity.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;

@Data
public class KingdeeOutStockDTO {
    @Panno(findType = PannoEnum.EQ,field = "fBillNo")
    private String billNo;

    @Panno(findType = PannoEnum.EQ,field = "fSoorDerno")
    private String soorDerno;
}
