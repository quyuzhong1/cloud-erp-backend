package com.erp.server.dmp.entity.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;

@Data
public class KingdeeSkuMongoDTO {

    @Panno(findType = PannoEnum.EQ,field = "FMaterialId")
    private String materialId;
}
