package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Cloud
 * @Classname SysCalendarDTO
 * @Description 系统日历参数
 * @Date 2023-01-06 17:08
 */

public class SysCalendarDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO{

        /**
         * 0 全部 1 日期， 2 月份， 3 年度  默认为 2
         */
        private Integer dateType;

        /**
         * 指定年月日
         */
        @NotNull(message = "请指定年,月,日至少一种类型")
        private LocalDate calendarDate;

        /**
         * 组织
         */
        private String organization;

        /**
         * 是否工作日
         */
        private Boolean isWorkDay;
        /**
         * 是否人工修改
         */
        private Boolean isManualSet;

    }

    @Data
    @NoArgsConstructor
    public static class SaveOrUpdateDTO{

        /**
         * 指定年月日
         */
        @NotEmpty(message = "日期列表不能为空")
        private List<LocalDate> calendarDateList;
        /**
         * 组织
         */
        private String organization;

        /**
         * 是否工作日
         */
        @NotNull(message = "日期类型不能为空")
        private Boolean isWorkDay;

        private String remark;

    }
}
