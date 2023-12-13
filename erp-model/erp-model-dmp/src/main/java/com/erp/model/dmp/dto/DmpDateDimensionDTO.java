package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 时间维度表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2023-12-08
*/
@Data
@NoArgsConstructor
public class DmpDateDimensionDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 订单时间
        */
        private LocalDateTime dateTime;

        /**
        * 数据时间名称（用于页面展示）
        */
        private String dateDay;

        /**
        * 数据时间名称（用于页面展示）
        */
        private String dateWeek;

        /**
        * 数据时间名称（用于页面展示）
        */
        private String dateMonth;

        /**
        * 数据时间名称（用于页面展示）
        */
        private String dateQuarter;

        /**
        * 数据时间名称（用于页面展示）
        */
        private String dateYear;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 订单时间
        */
        @NotNull(message = "订单时间不能为空")
        private LocalDateTime dateTime;

        /**
        * 数据时间名称（用于页面展示）
        */
        @NotBlank(message = "数据时间名称（用于页面展示）不能为空")
        @Size(max = 255,message = "数据时间名称（用于页面展示）最大长度不能超过255位")
        private String dateDay;

        /**
        * 数据时间名称（用于页面展示）
        */
        @NotBlank(message = "数据时间名称（用于页面展示）不能为空")
        @Size(max = 255,message = "数据时间名称（用于页面展示）最大长度不能超过255位")
        private String dateWeek;

        /**
        * 数据时间名称（用于页面展示）
        */
        @NotBlank(message = "数据时间名称（用于页面展示）不能为空")
        @Size(max = 255,message = "数据时间名称（用于页面展示）最大长度不能超过255位")
        private String dateMonth;

        /**
        * 数据时间名称（用于页面展示）
        */
        @NotBlank(message = "数据时间名称（用于页面展示）不能为空")
        @Size(max = 255,message = "数据时间名称（用于页面展示）最大长度不能超过255位")
        private String dateQuarter;

        /**
        * 数据时间名称（用于页面展示）
        */
        @NotBlank(message = "数据时间名称（用于页面展示）不能为空")
        @Size(max = 255,message = "数据时间名称（用于页面展示）最大长度不能超过255位")
        private String dateYear;


    }


}