package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 称重出库请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2023-12-13
 */
@Data
@NoArgsConstructor
@Builder
public class WeightingOutboundDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScanDTO {

        /**
         * 是否自动发货
         */
        @NotNull(message = "是否自动发货不能为空")
        private Boolean isAutoDelivery;

        /**
         * 业务单号：运单号或销售订单编号
         */
        @NotBlank(message = "业务单号不能为空")
        private String businessCode;

        /**
         * 称重重量
         */
        private BigDecimal weight;

        /**
         * 称重单位
         */

        private String weightUnit;
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
         * 称重状态
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
         * 称重重量
         */
        private BigDecimal weight;

        /**
         * 称重单位
         */
        private String weightUnit;


        /**
         * 中转状态
         * not 不需要  wait 待中转   already 已经中转
         */
        private String transferStatus;

        /**
         * 上传状态（订单）dict_basic：type=transferDeclareUploadStatus
         * enum:TransferDeclareUploadStatusEnum,waitUpload待上传,uploadFailure上传失败,uploadSuccess上传成功
         */
        private String orderUploadStatus;
    }
}