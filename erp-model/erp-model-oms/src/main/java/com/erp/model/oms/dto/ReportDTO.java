package com.erp.model.oms.dto;/**
 * @author Lambda
 * @Classname ReportDTO
 * @Description TODO
 * @Date 2023-09-01 10:04
 * @Created by yl
 */

import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-09-01 10:04
 */
@Data
@NoArgsConstructor
public class ReportDTO implements Serializable {

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class ProductSalesPagingParamDTO extends SortDTO {

        /**
         * 平台列表
         */
        private List<String> dictPlatformList;

        /**
         * 店铺id 列表
         */
        private List<String> shopIdList;

        /**
         * sku 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private List<LocalDateTime> skuCreateTimeList;

        /**
         * 平台sku no 卖家sku no
         * 产品 sku no
         */
        private String skuNo;

        /**
         * 订单创建时间
         */
        @NotNull(message = "订单创建时间不能为空")
        @Size(min = 2,message = "订单创建时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private List<LocalDateTime> orderCreateTimeList;


    }

    /**
     * 分
     */
    @Data
    @NoArgsConstructor
    public static class ProductSalesPagingViewDTO {

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名
         */
        private String shopName;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 卖家sku
         */
        private String sellerSkuNo;

        /**
         * 产品sku
         */
        private String productSkuNo;

        /**
         * 订单数量
         */
        private Integer orderCount;

        /**
         * 销售
         */
        private Integer qty;

        /**
         * 销售金额（人民币）
         */
        private BigDecimal amount;

        /**
         * 日均销量
         */
        private Integer avgQty;

        /**
         * 日均销售额
         */
        private BigDecimal avgAmount;
    }
}
