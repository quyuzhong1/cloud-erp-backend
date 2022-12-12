package com.erp.model.dmp.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GyyRefundDTO {

    @Panno(findType = PannoEnum.EQ,field = "refundCode")
    private String refundCode;

    @Panno(findType = PannoEnum.EQ,field = "platfromCode")
    private String platfromCode;

}
