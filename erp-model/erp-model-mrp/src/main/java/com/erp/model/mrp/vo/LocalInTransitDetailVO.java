package com.erp.model.mrp.vo;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LocalInTransitDetailVO {

    private String id;
    /**
     * 数量
     */
    private Integer qty;

    /**
     * 预计到货日期
     */
    private LocalDate planArrivalDate;

    /**
     * 预计可售日期
     */
    private LocalDate estimateSalesDate;

    /**
     * 来源id
     */
    private String sourceId;

    /**
     * 来源单号
     */
    private String sourceCode;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 仓库
     */
    @Dict(serviceCode = ServiceCodeNameEnum.WMS , queryFieldName = "id" , tableName = "warehouse")
    private String warehouseId;

    /**
     * 店铺在途明细
     */
    private Integer shopInTransitQty;
}
