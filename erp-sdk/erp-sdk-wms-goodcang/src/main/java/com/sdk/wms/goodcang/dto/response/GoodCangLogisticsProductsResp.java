package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangLogisticsProductsResp extends CleanBaseDTO implements Serializable {

    //物流产品编码
    @JSONField(name = "code")
    private String code;

    //物流产品中文名称
    @JSONField(name = "name")
    private String name;

    //物流产品英文名称
    @JSONField(name = "name_en")
    private String nameEn;

    //仓库代码
    @JSONField(name = "warehouse_code")
    private String warehouseCode;

    //erp仓库Id
    private String erpWarehouseId;

    //物流产品类型
    @JSONField(name = "type")
    private String type;

    //是否支持签名服务
    @JSONField(name = "is_signature")
    private Integer isSignature;

    //服务商代码
    @JSONField(name = "sp_code")
    private String spCode;

    //是否支持地址校验
    @JSONField(name = "address_validation_enabled")
    private Integer addressValidationEnabled;

    //是否指定到货时间
    @JSONField(name = "is_specify_arrival_time")
    private Integer isSpecifyArrivalTime;

    //支持的到货时间段
    @JSONField(name = "delivery_time_list")
    private List<String> deliveryTimeList;

    //是否卡派渠道
    @JSONField(name = "is_truck")
    private Integer isTruck;

    //业务类型
    @JSONField(name = "sm_business_type")
    private Integer smBusinessType;

    //是否打板 0否 1是
    @JSONField(name = "is_optional_board")
    private Integer isOptionalBoard;
}
