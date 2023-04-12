package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
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
    public static class ViewQcOverviewDTO {

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

    }

    @Data
    @NoArgsConstructor
    public static class ViewQcForPersonnelDTO {

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
        private Boolean isOverTime;

    }

    @Data
    @NoArgsConstructor
    public static class ViewQcForDocumentDTO {

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
