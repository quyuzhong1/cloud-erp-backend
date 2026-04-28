package com.sdk.wms.jitu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: wtr
 * @Date: 2026/4/27 18:41
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JituOverseasFbaOutboundCancelRequest implements Serializable {

    //货主编号
    private String customerid;
    //仓库编码
    private String warehouseCode;
    /**
     * 默认XSCK-销售出库
     * 入库单类型:
     * CGRK-采购入库
     * THRK-退货入库
     * DBRK-调拨入库
     * QTRK-其他入库
     */
    private String orderType;
    //单据编号
    private String orderCode;
    //取消原因
    private String cancelReason;
}
