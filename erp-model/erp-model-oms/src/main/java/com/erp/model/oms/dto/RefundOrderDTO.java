package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname RefundOrderDTO
 * @Date 2023-08-25 12:27
 * @Created by yl
 */
public class RefundOrderDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 单据单号
         */
        private String code;

        /**
         * 平台
         */
        private String dictPlatform;


        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 平台订单号
         */
        private String platformOrderNo;

        /**
         * 平台退款单号
         */
        private String platformRefundNo;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 销售单号
         */
        private String soId;
        /**
         * 状态
         */
        private String status;

        /**
         * 状态名
         */
        private String statusName;

        /**
         * 退款金额
         */
        private BigDecimal refundAmount;

        /**
         * 退款金额 + 币别
         */
        private String completeRefundAmount;

        /**
         * 退款人名币
         */
        private BigDecimal refundCnyAmount;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 退款原因
         */
        private String reason;

        /**
         * 平台退款时间
         */
        private LocalDateTime refundTime;

        /**
         * 平台创建时间
         */
        private LocalDateTime platformCreateTime;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售数量
         */
        private Integer saleQty;

        /**
         * 退款数量
         */
        private Integer refundQty;

        /**
         * 出库数量
         */
        private Integer outQty;;
    }
}
