package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 质检申请单主表请求响应实体
 * </p>
 *
 * @author will
 * @since 2026-03-20
*/
@Data
@NoArgsConstructor
public class QcApplicationSrmDTO implements Serializable {


    /**
     * tab列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class TabListParamDTO extends SortDTO {

        /**
         * 供应商id
         */
        private String supplierId;

    }

    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id【可排序】
        */
        private String  id;
        /**
         * 质检申请单明细id【可排序】
         */
        private String detailId;

        /**
        * 质检申请单号【可排序】
        */
        private String code;

        /**
        * 来源id【可排序】
        */
        private String sourceId;

        /**
        * 来源编码【可排序】
        */
        private String sourceCode;

        /**
        * 来源类型【可排序】
        */
        private String sourceType;

        /**
        * 单据状态【可排序】
        */
        private String approveStatus;
        /**
         * 单据状态名称
         */
        private String approveStatusName;
        /**
         * 最新审核人
         */
        private String approveUserName;

        /**
        * 审核完成时间【可排序】
        */
        private LocalDateTime approveTime;

        /**
        * 质检类型【可排序】
        */
        private String qcType;

        /**
         * 质检类型名称
         */
        private String qcTypeName;
        /**
         * SKU【可排序】
         */
        private String skuNo;

        /**
         * 产品名称【可排序】
         */
        private String productName;

        /**
         * 质检状态【可排序】
         */
        private String qcStatus;

        /**
         * 质检状态名称
         */
        private String qcStatusName;

        /**
         * 质检结果【可排序】
         */
        private String qcResult;

        /**
         * 质检结果名
         */
        private String qcResultName;

        /**
         * 申请质检数量【可排序】
         */
        private Integer qty;

        /**
         * 批次合格量【可排序】
         */
        private Integer batchQty;

        /**
         * 质检数量【可排序】
         */
        private Integer qcQty;

        /**
         * 质检合格数量【可排序】
         */
        private Integer qcGoodQty;

        /**
         * 质检不良数量【可排序】
         */
        private Integer qcBadQty;

        /**
        * 期望质检日期【可排序】
        */
        private LocalDate expectQcDate;

        /**
        * 备注【可排序】
        */
        private String remark;

        /**
        * 创建时间【可排序】
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称【可排序】
        */
        private String createUserName;

    }

}