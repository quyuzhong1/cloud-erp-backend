package com.erp.model.dmp.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;

@Data
public class KingdeeShopMongoDTO {

    @Panno(findType = PannoEnum.EQ,field = "fCustId")
    private String custId;
}
