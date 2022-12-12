package com.erp.server.dmp.entity.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KingdeeOrderDto {

    @Panno(findType = PannoEnum.EQ,field = "fBillNo")
    private String fBillNo;
}
