package com.erp.model.dmp.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KingdeeOutStockDTO {
    @Panno(findType = PannoEnum.EQ,field = "fBillNo")
    private String billNo;

    @Panno(findType = PannoEnum.EQ,field = "fSoorDerno")
    private String soorDerno;

    public KingdeeOutStockDTO(String fBillNo, String fSoorDerno) {
        this.billNo = fBillNo;
        this.soorDerno = fSoorDerno;
    }
}
