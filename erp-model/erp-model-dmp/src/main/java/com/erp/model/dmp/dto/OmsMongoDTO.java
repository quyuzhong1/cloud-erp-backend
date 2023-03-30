package com.erp.model.dmp.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmsMongoDTO {
    @Panno(findType = PannoEnum.EQ,field = "receivingCode")
    private String receivingCode;
}
