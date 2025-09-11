package com.erp.model.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 收款单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
*/
@Data
@NoArgsConstructor
public class SoReceiptDTO implements Serializable {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmountDTO {

        private String soId;

        private String soCode;

        private BigDecimal receiptAmount;

    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SoInfoAndReceiptDTO {

        /**
         * 订单id
         */
        private String soId;

        /**
         * 订单编号
         */
        private String soCode;

        /**
         * 销售组织id
         */
        private String salesOrgId;
        /**
         * 订单审核状态
         */
        private String approveStatus;

        /**
         * 订单审核状态名称
         */
        private String approveStatusName;

        /**
         * 剩余收款金额
         */
        private String remainReceiptAmount;

    }

     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;
         /**
          * 类型名称
          */
         private String tabFlagName;

         /**
         * 数量
         */
         private Integer count;

     }
     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
         * 页面高级查询
         */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
            * sqlMap 默认key default
        */
        private Map<String,String> sqlMap;
         /**
          * 勾选的id集合
          */
         private List<String> ids;
     }

    /**
     * 销售订单详情
     */
    @Data
    @NoArgsConstructor
    public static class SoViewDTO {

        /**
         * 收款单id
         */
        private String id;

        /**
         * 收款单明细id
         */
        private String detailId;

        /**
         * 收款单号
         */
        private String code;

        /**
         * 收款金额
         */
        private BigDecimal receiptAmount;

        /**
         * 收款日期
         */
        private LocalDate receiptDate;
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
         * 收款账号名称
         */
        private String receiptAccountName;
        /**
         * 付款流水号
         */
        private String paymentNo;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 付款单 附件
         */
        private List<AttachDTO> attachmentList;

        /**
         * 付款单明细 附件
         */
        private List<AttachDTO> detailAttachmentList;
    }
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
         * 明细
         */
        private String  detailId;
        /**
        * 单据编码
        */
        private String code;

        /**
        * 第三方单据编号
        */
        private String thirdCode;

        /**
        * 客户Id
        */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;
        /**
        * 审核状态
        */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 总收款金额
         */
        private BigDecimal totalReceiptAmount;

        /**
        * 币种
        */
        private String currency;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 销售id
         */
        private String soId;

        /**
         * 待收款金额
         */
        private BigDecimal remainReceiptAmount;

        /**
         * 收款金额
         */
        private BigDecimal receiptAmount;

        /**
         * 收款账号
         */
        private String receiptAccount;

        /**
         * 收款账号名称
         */
        private String receiptAccountName;
        /**
         * 收款日期
         */
        private LocalDate receiptDate;

        /**
         * 收款方式
         */
        private String dictReceiptMethod;

        /**
         * 收款方式
         */
        private String dictReceiptMethodName;

        /**
        * 是否入账
        */
        private Boolean isPosted;

        private String isPostedStr;
        /**
        * 入账账户
        */
        private String postedAccount;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人姓名
        */
        private String approveUserName;

        /**
        * 备注
        */
        private String remark;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
        * 创建人名称
        */
        private String createUserName;
        /**
         * 更新人名称
         */
        private String updateUserName;
    }

    /**
     * 查询销售订单收款信息dto
     */
    @Data
    @NoArgsConstructor
    public static class SoSearchDTO {

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 单个模糊搜索销售订单号
         */
        private String soCode;

        /**
         * 快粘贴销售订单号
         */
        private List<String> soCodeList;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

    }

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
         * 收款账号
         */
        private String receiptAccount;

        /**
         * 收款日期
         */
        private LocalDate receiptDate;
        /**
         * 收款方式
         */
        private String dictReceiptMethod;

        /**
         * 收款方式名称
         */
        private String dictReceiptMethodName;


        /**
         * 销售组织id
         */
        private String salesOrgId;
        /**
        * 单据编码
        */
        private String code;

        /**
        * 客户Id
        */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
        * 币种
        */
        private String currency;

        /**
        * 是否入账
        */
        private Boolean isPosted;

        /**
        * 入账账户
        */
        private String postedAccount;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 备注
        */
        private String remark;


        /**
         * 总收款金额
         */
        private BigDecimal totalReceiptAmount;

        /**
         * 附件
         */
        private List<AttachDTO> attachmentList;
        /**
         * 明细
         */
        private List<SoReceiptDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        @Valid
        private List<SoReceiptDetailDTO.AddDTO> detailList;

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
        /**
         * 明细
         */
        @Valid
        private List<SoReceiptDetailDTO.UpdateDTO> detailList;

        /**
         * 是否来源销售订单的修改
         */
        private boolean isFromSoUpdate = false;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 第三方单据编号
        */
        private String thirdCode;

        private String thirdSystem;
        /**
        * 客户Id
        */
        @NotBlank(message = "客户Id不能为空")
        @Size(max = 32,message = "客户Id最大长度不能超过32位")
        private String customerId;
        /**
         * 收款方式
         */
        @NotBlank(message = "收款方式不能为空")
        @Size(max = 255,message = "收款方式最大长度不能超过255位")
        private String dictReceiptMethod;
        /**
         * 收款金额
         */
        private BigDecimal receiptAmount;
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
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
        * 是否入账
        */
        @NotNull(message = "是否入账不能为空")
        private Boolean isPosted;

        /**
        * 入账账户
        */
        private String postedAccount;

        /**
         * 销售组织Id
         */
        private String salesOrgId;

        /**
         * 平台订单Id
         */
        private String platformOrderId;
        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源Id
        */
        private String sourceId;

        /**
        * 备注
        */
        private String remark;

        /**
         * 来源自第三方更新
         */
        private Boolean isFromPlatform = false;
        /**
         * 附件列表
         */
        private List<AttachDTO> attachmentList;

    }


}