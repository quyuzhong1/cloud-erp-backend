package com.erp.model.tms.dto;

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
 * 截单开船配置请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-15
*/
@Data
@NoArgsConstructor
public class TmsCfgSailingDTO implements Serializable {




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
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 物流商渠道id
        */
        private String logisticsChannelId;

        /**
        * 日期值（每...周，每...月）
        */
        private Integer dateValue;

        /**
        * 日期类型
        */
        private String dateType;

        /**
        * 开船日期（周、月）
        */
        private Integer startDate;

        /**
        * 开船日期时间
        */
        private LocalDateTime startTime;

        /**
        * 截单日期（周、月）
        */
        private Integer endDate;

        /**
        * 截单日期时间
        */
        private LocalDateTime endTime;


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
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        @Size(max = 19,message = "物流商id最大长度不能超过19位")
        private String logisticsSupplierId;

        /**
        * 物流商渠道id
        */
        @NotBlank(message = "物流商渠道id不能为空")
        @Size(max = 19,message = "物流商渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 日期值（每...周，每...月）
        */
        @NotNull(message = "日期值（每...周，每...月）不能为空")
        private Integer dateValue;

        /**
        * 日期类型
        */
        @NotBlank(message = "日期类型不能为空")
        @Size(max = 32,message = "日期类型最大长度不能超过32位")
        private String dateType;

        /**
        * 开船日期（周、月）
        */
        @NotNull(message = "开船日期（周、月）不能为空")
        private Integer startDate;

        /**
        * 开船日期时间
        */
        private LocalDateTime startTime;

        /**
        * 截单日期（周、月）
        */
        @NotNull(message = "截单日期（周、月）不能为空")
        private Integer endDate;

        /**
        * 截单日期时间
        */
        private LocalDateTime endTime;


    }


}