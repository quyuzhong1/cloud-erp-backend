package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class CfgRuleSafeDaysDTO {

    @Getter
    @Setter
    public static class ViewDTO {
        /**
         * 店铺id
         */
        private List<String> shopIdList;

        /**
         * 安全天数
         */
        private Integer safeDays;
    }


    @Getter
    @Setter
    public static class UpdateDTO {

        /**
         * id
         */
        private String id;
        /**
         * 店铺id
         */
        @Size(min = 1, message = "店铺不能为空")
        private List<String> shopIdList;

        /**
         * 安全天数
         */
        @Min(value = 0, message = "安全天数（天）最小值为0")
        @Max(value = 365, message = "安全天数（天）最大值为365")
        @NotNull(message = "安全天数（天）不能为空")
        private Integer safeDays;
    }
}
