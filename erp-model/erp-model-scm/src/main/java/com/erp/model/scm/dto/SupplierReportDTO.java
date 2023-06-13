package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商报表请求响应实体
 * @CreateTime: 2023-06-12  14:59
 * @Author: zhangchunlin
 */
@NoArgsConstructor
@Data
public class SupplierReportDTO implements Serializable {

    /**
     * 供应商报表查询 查询条件
     */
    @Data
    @NoArgsConstructor
    public static class PagingSearchParamDTO extends SortDTO {

        /**
         * 日期范围
         */
        private List<String> dateList;

        /**
         * 供应商名称
         */
        private List<String> supplierNameList;

        /**
         * 内外检类型  接口地址：/scm/common/enumDropDown?type=QcInsideType
         */
        private String qcType;

    }

    /**
     * 供应商报表分页列表
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 供应商id
         */
        private String id;

        /**
         * 供应商编码
         */
        private String code;

        /**
         * 供应商名称
         */
        private String name;

        /**
         * 供应商禁用状态
         */
        private String supplierDisabled;

        /**
         * 采购订单量
         */
        private Integer purchaseCount;

        /**
         * 采购总数
         */
        private Integer purchaseQty;

        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount;

        /**
         * 收货批次
         */
        private Integer receivedCount;

        /**
         * 入库批次
         */
        private Integer instockCount;

        /**
         * 质检退货批次
         */
        private Integer qcReturnCount;

        /**
         * 已收货量
         */
        private Integer receivedQty;

        /**
         * 已入库量
         */
        private Integer instockQty;

        /**
         * 次品数量
         */
        private Integer defectiveQty;

        /**
         * 质检退货量
         */
        private Integer qcReturnQty;

        /**
         * 合格率（批次）
         */
        private BigDecimal passRateCount;

        /**
         * 合格率（量）
         */
        private BigDecimal passRateQty;

    }

    /**
     * 供应商报表导出 查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ExportSearchParamDTO extends PagingSearchParamDTO{

        /**
         * 勾选的供应商id集合
         */
        private List<String> ids;

    }

}