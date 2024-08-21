package com.sdk.wms.antu.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AntuLogisticsProductsResp extends CleanBaseDTO implements Serializable {

    //运输方式代码
    @JSONField(name = "code")
    private String code;

    //运输方式中文名称
    @JSONField(name = "name")
    private String name;

    //运输方式英文名称
    @JSONField(name = "nameEn")
    private String nameEn;

    //仓库代码
    @JSONField(name = "warehouseCode")
    private String warehouseCode;

    //erp仓库id
    private String ErpWarehouseId;

}
