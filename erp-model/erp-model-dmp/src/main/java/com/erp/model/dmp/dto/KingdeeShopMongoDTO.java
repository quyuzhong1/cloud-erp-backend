package com.erp.model.dmp.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KingdeeShopMongoDTO {

    @Panno(findType = PannoEnum.EQ,field = "fCustId")
    private String custId;

    public KingdeeShopMongoDTO(String fCustId) {
        this.custId = fCustId;
    }
}
