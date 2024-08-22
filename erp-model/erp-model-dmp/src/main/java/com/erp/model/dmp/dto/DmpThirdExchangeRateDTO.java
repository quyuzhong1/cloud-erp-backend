package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * 第三方汇率请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-19
*/
@Data
@NoArgsConstructor
public class DmpThirdExchangeRateDTO implements Serializable {




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
        * 来源系统
        */
        private String sourceSystem;

        /**
        * 生效日期
        */
        private LocalDateTime settlementDateBegin;

        /**
        * 失效日期
        */
        private LocalDateTime settlementDateEnd;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 源币种
        */
        private String sourceCurrencyCode;

        /**
        * 目标币种
        */
        private String targetCurrencyCode;

        /**
        * 汇率类型
        */
        private String type;

        /**
        * 间接汇率
        */
        private BigDecimal indirectExchangeRate;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 审核日期
        */
        private LocalDateTime approveDate;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 禁用日期
        */
        private LocalDateTime disabledDate;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


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
        * 来源系统
        */
        @NotBlank(message = "来源系统不能为空")
        @Size(max = 32,message = "来源系统最大长度不能超过32位")
        private String sourceSystem;

        /**
        * 生效日期
        */
        private LocalDateTime settlementDateBegin;

        /**
        * 失效日期
        */
        private LocalDateTime settlementDateEnd;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 源币种
        */
        @NotBlank(message = "源币种不能为空")
        @Size(max = 40,message = "源币种最大长度不能超过40位")
        private String sourceCurrencyCode;

        /**
        * 目标币种
        */
        @NotBlank(message = "目标币种不能为空")
        @Size(max = 40,message = "目标币种最大长度不能超过40位")
        private String targetCurrencyCode;

        /**
        * 汇率类型
        */
        @NotBlank(message = "汇率类型不能为空")
        @Size(max = 64,message = "汇率类型最大长度不能超过64位")
        private String type;

        /**
        * 间接汇率
        */
        @NotNull(message = "间接汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "间接汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal indirectExchangeRate;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 40,message = "来源id最大长度不能超过40位")
        private String sourceId;

        /**
        * 审核日期
        */
        private LocalDateTime approveDate;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 禁用日期
        */
        private LocalDateTime disabledDate;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}