package com.erp.model.plm.dto;

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
 * 合同返还约定请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldRefundAgreementDTO implements Serializable {




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
        * 模具id
        */
        private String mouldDetailId;

        /**
        * 是否费用返还
        */
        private Boolean isNeedRefund;

        /**
        * 返还标准
        */
        private String refundStandard;

        /**
        * 退款单量
        */
        private Integer refundOrderQty;

        /**
        * 返还金额
        */
        private BigDecimal refundAmount;

        /**
        * 费用返还状态
        */
        private String refundStatus;


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
        * 模具id
        */
        @NotBlank(message = "模具id不能为空")
        @Size(max = 19,message = "模具id最大长度不能超过19位")
        private String mouldDetailId;

        /**
        * 是否费用返还
        */
        @NotNull(message = "是否费用返还不能为空")
        private Boolean isNeedRefund;

        /**
        * 返还标准
        */
        @NotBlank(message = "返还标准不能为空")
        @Size(max = 255,message = "返还标准最大长度不能超过255位")
        private String refundStandard;

        /**
        * 退款单量
        */
        @NotNull(message = "退款单量不能为空")
        private Integer refundOrderQty;

        /**
        * 返还金额
        */
        @NotNull(message = "返还金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "返还金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal refundAmount;

        /**
        * 费用返还状态
        */
        @NotBlank(message = "费用返还状态不能为空")
        @Size(max = 255,message = "费用返还状态最大长度不能超过255位")
        private String refundStatus;


    }


}