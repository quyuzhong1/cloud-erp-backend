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
public class JituOverseasInboundCreateRequest implements Serializable {
    //仓库编码
    private String warehouseCode;
    //客户订单号
    private String entryOrderCode;
    //货主编号
    private String customerid;
    //来源订单号
    private String erpOrderCode;
    //订单来源系统
    private String sourceSystem;
    //外部业务单号
    private String outBizNo;
    //经营公司代码
    private String storerKey;
    /**
     * 入库单类型:
     * CGRK-采购入库
     * THRK-退货入库
     * DBRK-调拨入库
     * QTRK-其他入库
     */
    private String orderType;
    //计划到达日期:YYYY-MM-DD
    private LocalDate expectStartTime;
    //客退运单号
    private String trackNo;
    //供应商编码
    private String supplierCode;
    //出库仓库编码
    private String sourceWarehouseCode;
    //调出门店编号
    private String sourceStoreCode;
    //入库单备注
    private String remark;
    //预留字段
    private String reserved1;

    private List<Item> items;

    @Data
    @Builder
    public static class Item {
        //入库单行号
        private String LineNo;
        //货品编码
        private String itemCode;
        //计划入库数量
        private Integer quantity;
        //库存类型：ZP/CC，默认ZP
        private String inventoryType;
        //生产日期：YYYY-MM-DD
        private String productDate;
        //失效日期：YYYY-MM-DD
        private String expirationDate;
        //生产批号
        private String produceCode;
        //批次编码
        private String batchCode;
        //备注
        private String remark;
        //预留字段
        private JSONObject reserved1;
    }
}
