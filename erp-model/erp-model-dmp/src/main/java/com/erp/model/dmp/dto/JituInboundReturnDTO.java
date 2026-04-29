package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JituInboundReturnDTO implements Serializable {
    //消息ID
    private String requestId;
    //仓库编码
    private String warehouseCode;
    //来源平台
    private String sourcePlatform;
    //平台仓库类型
    private String warehousePlatformType;
    //货主编号
    private String customerid;
    //经营公司代码
    private String storerKey;
    //客户订单号
    private String entryOrderCode;
    //入库单号
    private String entryOrderId;
    //客退运单号
    private String returnWaybillCode;
    //订单来源系统
    private String sourceSystem;
    //外部业务单号
    private String outBizNo;
    /**
     * 入库单类型:
     * CGRK-采购入库
     * THRK-退货入库
     * DBRK-调拨入库
     * QTRK-其他入库
     */
    private String orderType;
    //确认类型 支持入库单多次收货(多次收货后确认时: 0:表示入库单最终状态确认，全量回传； 1:表示入库单中间状态确认，增量回传； 特殊情况:同一入库单;如果先收到0;后又收到1;允许修改收货的数量)
    private Integer confirmType;
    //入库单备注
    private String remark;
    //入库单状态 (NEW-未开始处理; ACCEPT-仓库接单; PARTFULFILLED-部分收货完成; FULFILLED-收货完成; EXCEPTION-异常; CANCELED-取消; 只传英文编码)
    private String status;
    //操作时间(YYYY-MM-DD HH:MM:SS;当status=FULFILLED;operateTime为上架时间)
    private LocalDateTime operateTime;
    //预留字段
    private String reserved1;

    private List<Item> orderLines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        //操作时间(YYYY-MM-DD HH:MM:SS;当status=FULFILLED;operateTime为上架时间)
        private LocalDateTime operateTime;
        //入库单行号
        private String LineNo;
        //货品编码
        private String itemCode;
        //货品名称
        private String itemName;
        //计划入库数量
        private Integer quantity;
        //实收商品数量
        private Integer actualQty;
        //库存类型：ZP/CC，默认ZP
        private String inventoryType;
        //生产日期：YYYY-MM-DD
        private LocalDate productDate;
        //失效日期：YYYY-MM-DD
        private LocalDate expireDate;
        //生产批号
        private String produceCode;
        //批次编码
        private String batchCode;
        //预留字段
        private String reserved1;
        //唯一码列表  货品唯一码
        private List<String> snList;
        //批次列表
        private List<Batche> batches;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Batche {
            //实收商品数量 当前货品的最小单位实收数量，根据上架任务进行分批次回传/或整单回传。
            private Integer actualQty;
            //库存类型 ZP/CC,默认ZP
            private String inventoryType;
            //生产日期
            private LocalDate productDate;
            //过期日期
            private LocalDate expireDate;
            //生产批号
            private String produceCode;
            //批次编码
            private String batchCode;
        }
    }


}