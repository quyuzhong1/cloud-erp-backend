package com.erp.model.tms.dto;

import java.math.BigDecimal;
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
 * 中转费用分摊明细请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class TransferDeclareCostAllocationDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 账单金额
        */
        private BigDecimal billAmount;

        /**
        * 费用类型
        */
        private String feeType;

        /**
        * 费用分摊类型
        */
        private String feeAllocationType;

        /**
        * 分摊金额
        */
        private BigDecimal allocatedAmount;

        /**
        * 分摊币种
        */
        private String allocatedCurrency;

        /**
        * 费用分摊方式
        */
        private BigDecimal productAllocatedAmount;

        /**
        * 重量分摊方式
        */
        private String weightAllocationType;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 账单金额
        */
        @NotNull(message = "账单金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "账单金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal billAmount;

        /**
        * 费用类型
        */
        @NotBlank(message = "费用类型不能为空")
        @Size(max = 50,message = "费用类型最大长度不能超过50位")
        private String feeType;

        /**
        * 费用分摊类型
        */
        @NotBlank(message = "费用分摊类型不能为空")
        @Size(max = 50,message = "费用分摊类型最大长度不能超过50位")
        private String feeAllocationType;

        /**
        * 分摊金额
        */
        @NotNull(message = "分摊金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "分摊金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allocatedAmount;

        /**
        * 分摊币种
        */
        @NotBlank(message = "分摊币种不能为空")
        @Size(max = 50,message = "分摊币种最大长度不能超过50位")
        private String allocatedCurrency;

        /**
        * 费用分摊方式
        */
        @NotNull(message = "费用分摊方式不能为空")
        @Digits(integer = 12, fraction = 4, message = "费用分摊方式整数位不能超过12位，小数位不能超过4位")
        private BigDecimal productAllocatedAmount;

        /**
        * 重量分摊方式
        */
        @NotBlank(message = "重量分摊方式不能为空")
        @Size(max = 50,message = "重量分摊方式最大长度不能超过50位")
        private String weightAllocationType;


    }


}