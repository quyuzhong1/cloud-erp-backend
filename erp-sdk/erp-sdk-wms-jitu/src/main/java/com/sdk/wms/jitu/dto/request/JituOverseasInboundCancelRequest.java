package com.sdk.wms.jitu.dto.request;

import cn.hutool.json.JSONObject;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author zdy
 * @ClassName JituOverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class JituOverseasInboundCancelRequest implements Serializable {
    //货主编号
    private String customerid;
    //仓库编码
    private String warehouseCode;
    /**
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
