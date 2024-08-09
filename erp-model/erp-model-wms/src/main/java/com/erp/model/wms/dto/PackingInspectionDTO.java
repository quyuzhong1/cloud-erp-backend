package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 包装验货请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2023-12-13
 */
@Data
@NoArgsConstructor
@Builder
public class PackingInspectionDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScanDTO {
        /**
         * 操作类型，eg:按单号+SKU验货，按单号验货
         * {@link com.erp.model.wms.enums.PackingInspectionOperationEnum}
         */
        @NotBlank(message = "操作类型不能为空")
        private String operationType;

        /**
         * 是否自动出库
         */
        @NotNull(message = "是否自动出库不能为空")
        private Boolean isAutoOut;


        /**
         * 业务单号：运单号或销售订单编号
         */
        @NotBlank(message = "业务单号不能为空")
        private String businessCode;

        /**
         * 扫描数量
         */
        private Integer scanQty;

        /**
         * sku编号
         */
        private String skuNo;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViewDTO {

        /**
         * 发货单id
         */
        private String id;

        /**
         * 纸张大小
         */
        private String paperSize;

        /**
         * 打印机名称
         */
        private String printerName;

        /**
         * 验货状态
         */
        private Boolean status;

        /**
         * 订单编号
         */
        private String code;

        /**
         * 运单号
         */
        private String transportNo;
        /**
         * 跟踪号
         */
        private String trackNo;

        /**
         * sku种类数
         */
        private Integer skuSpeciesQty;

        /**
         * sku总数量
         */
        private Integer skuTotalQty;

        /**
         * 中转状态 dict_basic：type=transferStatus
         * not 不需要  wait 待中转   already 已经中转
         */
        private String transferStatus;

        /**
         * 上传状态（订单）dict_basic：type=transferDeclareUploadStatus
         * enum:TransferDeclareUploadStatusEnum,waitUpload待上传,uploadFailure上传失败,uploadSuccess上传成功
         */
        private String orderUploadStatus;

        private List<ScanSkuInfo> waitScanSkuList;

        private List<ScanSkuInfo> scannedSkuList;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ScanSkuInfo {

            /**
             * skuId
             */
            private String skuId;

            /**
             * sku图片链接
             */
            private String skuImageUrl;

            /**
             * sku编号
             */
            private String skuNo;

            /**
             * ean码
             */
            private String ean;

            /**
             * 待扫描数量
             */
            private Integer waitScanQty;

            /**
             * 已扫描数量
             */
            private Integer scannedQty;

            /**
             * 销售数量
             */
            private Integer saleQty;

            /**
             * 仓位
             */
            private String warehouseLocation;

            /**
             * 产品名称
             */
            private String productName;
        }
    }
}