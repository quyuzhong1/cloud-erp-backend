package com.erp.model.srm.dto;

import java.math.BigDecimal;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 销量共享表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-06-18
*/
@Data
@NoArgsConstructor
public class SalesSharingDTO implements Serializable {


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商编码
         */
        private String supplierCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 产品图片
         */
        private String productImage;

        /**
         * 产品图片url
         */
        private String productImageUrl;

        /**
         * sku_id
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
         */
        private Integer saleState;
        private String saleStateName;

        /**
         * 可销库存
         */
        private Integer saleableStock;

        /**
         * 原始日均销量
         */
        private Integer dailySales;

        /**
         * 近3日销量
         */
        private Integer salesLast3Days;

        /**
         * 近7日销量
         */
        private Integer salesLast7Days;

        /**
         * 近30日销量
         */
        private Integer salesLast30Days;

        /**
         * 近60日销量
         */
        private Integer salesLast60Days;

        /**
         * 近90日销量
         */
        private Integer salesLast90Days;

        /**
         * 原始销量比例
         */
        private BigDecimal salesRatio;

        /**
         * 可销天数
         */
        private Integer saleableDays;
        /**
         * 是否需要进行通知
         */
        private Boolean needNotice;
        /**
         * 通知内容
         */
        private String noticeContent;

    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }


    /**
     * 分页列表查询参数
     */
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

        /**
         * 主键id
         */
        private List<String> ids;

        /**
         * 供应商id
         */
        private String supplierId;



    }

}