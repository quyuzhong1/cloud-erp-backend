package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 质量检测时效表
 * @date 2023/4/12 11:20
 */
@Data
@NoArgsConstructor
public class QcEffectivenessDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class CommonSearchParamDTO {
        /**
         * 时间集合
         */
        private List<LocalDate> dateList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewQcOverviewDetailDTO {
        /**
         * 类型（总计、已质检、待质检、免检）
         */
        private String type;

        /**
         * 数量
         */
        private Integer count;

    }

    @Data
    @NoArgsConstructor
    public static class ViewQcOverviewDTO {

        /**
         * 完成率（%）
         */
        private String completionRate;

        /**
         * 质检数量集合
         */
        private List<ViewQcOverviewDetailDTO> list;

    }

    @Data
    @NoArgsConstructor
    public static class ViewQcTrendSearchParamDTO extends CommonSearchParamDTO {
        /**
         * 类型（day日、week周、month月）
         */
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class ViewQcTrendDTO {

        /**
         * 日期集合
         */
        private List<LocalDate> dateList;

        /**
         * 待质检数量集合
         */
        private List<Integer> waitQcQtyList;

         /**
          * 质检中数量集合
          */
         private List<Integer> inQcQtyList;

         /**
          * 质检（已质检、免检）数量集合
          */
         private List<Integer> qcQtyList;

         /**
          * 已取消数量集合
          */
         private List<Integer> cancelQtyList;

    }

    @Data
    @NoArgsConstructor
    public static class ViewQcForPersonnelDTO {
        /**
         * 质检员
         */
        private String qcUserName;

        /**
         * 质检总数量
         */
        private Integer   qcTotalQty;

        /**
         * 完成质检数量
         */
        private Integer   qcFinishQty;

        /**
         * 待质检数量
         */
        private Integer    qcWaitQty;

        /**
         * 免检数量
         */
        private Integer    qcFreeQty;

        /**
         * 质检及时
         */
        private Integer   qcTimelyQty;

        /**
         * 质检超时（已完成）
         */
        private Integer   qcTimeOutQty;

        /**
         * 质检超时（未完成）
         */
        private Integer  unQcTimeOutQty;

    }

    @Data
    @NoArgsConstructor
    public static class ViewQcForDocumentSearchParamDTO extends CommonSearchParamDTO {

        /**
         * 质检状态(待质检、已质检、免检、已取消)
         */
        private List<String> statusList;

        /**
         * 是否超时（true是，false否）
         */
        private Boolean isTimeOut;

    }

    @Data
    @NoArgsConstructor
    public static class ViewQcForDocumentDTO {
        /**
         * 质检时间
         */
        private LocalDate qcDate;

        /**
         * 质检员
         */
        private String qcUserName;

        /**
         * 质检单号
         */
        private String qcCode;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 质检数量
         */
        private Integer qcQty;

        /**
         * 质检状态
         */
        private String qcStatusName;

        /**
         * 质检结束时间
         */
        private LocalDateTime qcEndTime;

        /**
         * 质检耗时
         */
        private String qcUseTime;

        /**
         * 质检预警
         */
        private String warnRemark;

    }

    @Data
    @NoArgsConstructor
    public static class ExportExcelSearchParamDTO extends CommonSearchParamDTO {

        /**
         * 导出类型（personnel按人员、document按单据）
         */
        private String type;

        /**
         * 质检状态(待质检、已质检、免检、已取消)
         */
        private List<String> statusList;

        /**
         * 是否超时（true是，false否）
         */
        private Boolean isOverTime;

    }
}
