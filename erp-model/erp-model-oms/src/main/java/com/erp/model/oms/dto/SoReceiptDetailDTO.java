package com.erp.model.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.common.business.dto.AttachDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 收款单明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
*/
@Data
@NoArgsConstructor
public class SoReceiptDetailDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 明细id
        */
        private String  id;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 销售id
        */
        private String soId;

        /**
        * 销售单号
        */
        private String soCode;
        /**
         * 剩余收款金额
         */
        private String remainReceiptAmount;

        /**
         * 收款金额
         */
        private BigDecimal receiptAmount;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态 name
         */
        private String approveStatusName;

        /**
         * 附件
         */
        private List<AttachDTO> attachmentList;

        /**
        * 付款流水号
        */
        private String paymentNo;

        /**
        * 收款方式
        */
        private String dictReceiptMethod;

        /**
         * 收款方式名称
         */
        private String dictReceiptMethodName;
        /**
        * 收款账号
        */
        private String receiptAccount;

        /**
        * 收款日期
        */
        private LocalDate receiptDate;

        /**
        * 备注
        */
        private String remark;

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
        private String mainId;

        /**
        * 销售id
        */
        @NotBlank(message = "销售id不能为空")
        @Size(max = 255,message = "销售id最大长度不能超过255位")
        private String soId;

        /**
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 255,message = "销售单号最大长度不能超过255位")
        private String soCode;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 付款流水号
        */
        @NotBlank(message = "付款流水号不能为空")
        @Size(max = 255,message = "付款流水号最大长度不能超过255位")
        private String paymentNo;

        /**
         * 附件列表
         */
        private List<AttachDTO> attachmentList;

        /**
        * 收款方式
        */
        @NotBlank(message = "收款方式不能为空")
        @Size(max = 255,message = "收款方式最大长度不能超过255位")
        private String dictReceiptMethod;

        /**
        * 收款账号
        */
        @NotBlank(message = "收款账号不能为空")
        @Size(max = 255,message = "收款账号最大长度不能超过255位")
        private String receiptAccount;

        /**
        * 收款日期
        */
        private LocalDate receiptDate;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 收款金额
        */
        @NotNull(message = "收款金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "收款金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal receiptAmount;


    }


}