package com.erp.model.mrp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 发货计划请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class DeliverySuggestDTO implements Serializable {




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
        * 编码
        */
        private String code;

        /**
        * 创建类型（auto系统，manual人工）
        */
        private String createType;

        /**
        * 建议发货量
        */
        private Integer suggestDeliveryQty;

        /**
        * 建议发货日期
        */
        private LocalDate suggestDeliveryDate;

        /**
        * 物流方式,LogisticsMethodEnum枚举
        */
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        private Integer logisticsDays;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 物流成本
        */
        private BigDecimal logisticsCost;

        /**
        * 作废状态
        */
        private String invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;


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
        * 创建类型（auto系统，manual人工）
        */
        @NotBlank(message = "创建类型（auto系统，manual人工）不能为空")
        @Size(max = 32,message = "创建类型（auto系统，manual人工）最大长度不能超过32位")
        private String createType;

        /**
        * 建议发货量
        */
        @NotNull(message = "建议发货量不能为空")
        private Integer suggestDeliveryQty;

        /**
        * 建议发货日期
        */
        private LocalDate suggestDeliveryDate;

        /**
        * 物流方式,LogisticsMethodEnum枚举
        */
        @NotBlank(message = "物流方式,LogisticsMethodEnum枚举不能为空")
        @Size(max = 32,message = "物流方式,LogisticsMethodEnum枚举最大长度不能超过32位")
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        @NotNull(message = "物流时效（天）不能为空")
        private Integer logisticsDays;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 物流成本
        */
        @NotNull(message = "物流成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "物流成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal logisticsCost;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;


    }


}