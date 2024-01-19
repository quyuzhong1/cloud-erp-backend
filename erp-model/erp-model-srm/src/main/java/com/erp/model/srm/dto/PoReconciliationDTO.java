package com.erp.model.srm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 采购对账单请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class PoReconciliationDTO implements Serializable {




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
        * 对账单号
        */
        private String code;

        /**
        * 对账状态
        */
        private String status;

        /**
        * 对账开始日期
        */
        private LocalDateTime startDate;

        /**
        * 对账结束日期
        */
        private LocalDateTime endDate;

        /**
        * 结算组织id
        */
        private String settleOrgId;

        /**
        * 结算组织名称
        */
        private String settleOrgName;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 对账金额
        */
        private BigDecimal amount;

        /**
        * 币别
        */
        private String currency;

        /**
        * 供方对账人id
        */
        private String supplierReconciliationUserId;

        /**
        * 供方对账人名称
        */
        private String supplierReconciliationUserName;

        /**
        * 采方对账人id
        */
        private String purchaseReconciliationUserId;

        /**
        * 采方对账人名称
        */
        private String purchaseReconciliationUserName;

        /**
        * 生成对账日期
        */
        private LocalDate reconciliationDate;

        /**
        * 供方确认日期
        */
        private LocalDate supplierConfirmDate;

        /**
        * 采方确认日期
        */
        private LocalDate purchaseConfirmDate;

        /**
        * 收到单据日期
        */
        private LocalDate receiveDate;

        /**
        * 供方确认人id
        */
        private String supplierConfirmUserId;

        /**
        * 供方确认人名称
        */
        private String supplierConfirmUserName;

        /**
        * 采方确认人id
        */
        private String purchaseConfirmUserId;

        /**
        * 采方确认人名称
        */
        private String purchaseConfirmUserName;


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
        * 对账状态
        */
        @NotBlank(message = "对账状态不能为空")
        @Size(max = 32,message = "对账状态最大长度不能超过32位")
        private String status;

        /**
        * 对账开始日期
        */
        private LocalDateTime startDate;

        /**
        * 对账结束日期
        */
        private LocalDateTime endDate;

        /**
        * 结算组织id
        */
        @NotBlank(message = "结算组织id不能为空")
        @Size(max = 19,message = "结算组织id最大长度不能超过19位")
        private String settleOrgId;

        /**
        * 结算组织名称
        */
        @NotBlank(message = "结算组织名称不能为空")
        @Size(max = 100,message = "结算组织名称最大长度不能超过100位")
        private String settleOrgName;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 供应商名称
        */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 100,message = "供应商名称最大长度不能超过100位")
        private String supplierName;

        /**
        * 对账金额
        */
        @NotNull(message = "对账金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "对账金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 供方对账人id
        */
        @NotBlank(message = "供方对账人id不能为空")
        @Size(max = 19,message = "供方对账人id最大长度不能超过19位")
        private String supplierReconciliationUserId;

        /**
        * 供方对账人名称
        */
        @NotBlank(message = "供方对账人名称不能为空")
        @Size(max = 100,message = "供方对账人名称最大长度不能超过100位")
        private String supplierReconciliationUserName;

        /**
        * 采方对账人id
        */
        @NotBlank(message = "采方对账人id不能为空")
        @Size(max = 32,message = "采方对账人id最大长度不能超过32位")
        private String purchaseReconciliationUserId;

        /**
        * 采方对账人名称
        */
        @NotBlank(message = "采方对账人名称不能为空")
        @Size(max = 100,message = "采方对账人名称最大长度不能超过100位")
        private String purchaseReconciliationUserName;

        /**
        * 生成对账日期
        */
        private LocalDate reconciliationDate;

        /**
        * 供方确认日期
        */
        private LocalDate supplierConfirmDate;

        /**
        * 采方确认日期
        */
        private LocalDate purchaseConfirmDate;

        /**
        * 收到单据日期
        */
        private LocalDate receiveDate;

        /**
        * 供方确认人id
        */
        @NotBlank(message = "供方确认人id不能为空")
        @Size(max = 19,message = "供方确认人id最大长度不能超过19位")
        private String supplierConfirmUserId;

        /**
        * 供方确认人名称
        */
        @NotBlank(message = "供方确认人名称不能为空")
        @Size(max = 100,message = "供方确认人名称最大长度不能超过100位")
        private String supplierConfirmUserName;

        /**
        * 采方确认人id
        */
        @NotBlank(message = "采方确认人id不能为空")
        @Size(max = 19,message = "采方确认人id最大长度不能超过19位")
        private String purchaseConfirmUserId;

        /**
        * 采方确认人名称
        */
        @NotBlank(message = "采方确认人名称不能为空")
        @Size(max = 100,message = "采方确认人名称最大长度不能超过100位")
        private String purchaseConfirmUserName;


    }


}