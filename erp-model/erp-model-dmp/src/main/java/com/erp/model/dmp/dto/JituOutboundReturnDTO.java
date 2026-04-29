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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JituOutboundReturnDTO implements Serializable {
    private String warehousePlatformType;
    private String sourcePlatform;
    //消息ID
    private String requestId;
    //仓库编码
    private String warehouseCode;
    //仓库名称
    private String warehouseName;
    //货主编号
    private String customerCode;
    //经营公司代码
    private String storerKey;
    //客户订单号
    private String providerOrderId;
    //出库单号
    private String deliveryOrderCode;
    //来源平台
    private String source;
    //平台单号
    private String platformNumber;
    //订单来源系统
    private String sourceSystem;
    //外部业务单号
    private String outBizNo;
    //出库时间
    private LocalDateTime scanTime;
    //物流服务商编码
    private String logisticCode;
    //运单号-母单号
    private String trackingNumber;
    //预留字段
    private String reserved1;
    //包裹列表
    private List<Package> packages;
    //订单行列表
    private List<Item> orderLines;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Package {
        //物流服务商编码
        private String logisticCode;
        //物流服务商名称
        private String logisticName;
        //运单号
        private String trackingNumber;
        //包裹号
        private String packageCode;
        //包裹-商品子列表
        private List<PackageItem> packagesitemsList;
        //包裹-包材子列表
        private List<PackageMaterial> packageMaterialList;
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PackageItem {
            //货品编号
            private String itemCode;
            //数量
            private Integer quantity;
        }
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PackageMaterial {
            //包材编号
            private String Materialtype;
            //包材数量：1
            private Integer quantity;
        }
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        //入库单行号
        private Integer lineNo;
        //出库时间
        private LocalDateTime scanTime;
        //货品编码
        private String itemCode;
        //计划数量：最小单位的总数量
        private Integer number;
        //实际出库量：最小单位数量
        private Integer actualQty;
        //库存类型：ZP/CC，默认ZP
        private String inventoryType;
        //批次编码
        private String batchCode;
        //生产批号
        private String produceCode;
        //生产日期：YYYY-MM-DD
        private LocalDate productDate;
        //备注
        private String remark;
        //预留字段
        private String reserved1;
        //唯一码列表  货品唯一码
        private List<String> snList;
        //批次列表
        private List<Batche> batches;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Batche {
            //实收商品数量 当前货品的最小单位实收数量，根据上架任务进行分批次回传/或整单回传。
            private Integer actualQty;
            //库存类型 ZP/CC,默认ZP
            private String inventoryType;
            //批次编码
            private String batchCode;
            //生产日期
            private LocalDate productDate;
            //过期日期
            private LocalDate expireDate;
            //生产批号
            private String produceCode;
            //供应商编码
            private String supplierCode;
            //供应商名称
            private String supplierName;

        }
    }


}