package com.erp.model.srm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
         * 主键id
         */
        private String id;
    }


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 ,(toBeSupplierConfirm待供方确认（srm待我确认）,toBePurchaseConfirm待采方确认（scm待我确认）,confirm已确认,received已收单据)
         */
        private String tabFlag;

        /**
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 对账单主键id
         */
        private String id;

        /**
         * 对账单号【可排序】
         */
        private String code;

        /**
         * 对账状态【可排序】,/srm/drop/down/dict/list?key=poReconciliationStatus
         */
        private String status;

        /**
         * 对账状态名称
         */
        private String statusName;

        /**
         * 对账开始日期【可排序】
         */
        private LocalDate startDate;

        /**
         * 对账结束日期【可排序】
         */
        private LocalDate endDate;

        /**
         * 对账周期
         */
        private String cycle;

        /**
         * 结算组织【可排序】
         */
        private String settleOrgName;

        /**
         * 供应商id【可排序】
         */
        private String supplierId;

        /**
         * 供应商名称【可排序】
         */
        private String supplierName;

        /**
         * 对账金额【可排序】
         */
        private BigDecimal amount;

        /**
         * 币种【可排序】
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 供方对账人【可排序】
         */
        private String supplierConfirmUserName;

        /**
         * 采方对账人【可排序】
         */
        private String purchaseConfirmUserName;

        /**
         * 生成对账日期【可排序】
         */
        private LocalDate reconciliationDate;

        /**
         * 供方确认日期【可排序】
         */
        private LocalDate supplierConfirmDate;

        /**
         * 采方确认日期【可排序】
         */
        private LocalDate purchaseConfirmDate;

        /**
         * 收到单据日期【可排序】
         */
        private LocalDate receiveDate;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
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
        * 对账单号
        */
        private String code;

        /**
        * 对账状态,/srm/drop/down/dict/list?key=poReconciliationStatus
        */
        private String status;

        /**
         * 对账状态名称
         */
        private String statusName;

        /**
        * 结算组织id,/api/sys/company/list
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
         * 预付金额
         */
        private BigDecimal totalPrepayAmount;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
        * 币别
        */
        private String currency;

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
         * 备注
         */
        private String remark;
    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO  {

        /**
         * 标题
         */
        private String titil;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 联系人
         */
        private String contactName;

        /**
         * 联系方式
         */
        private String contactTelNumber;

        /**
         * 结算方式名称
         */
        private String settleDictName;

        /**
         * 付款条件明名称
         */
        private String paymentConditionName;

        /**
         * 采购跟单员名称
         */
        private String poFollowerName;

        /**
         * 采购跟单员电话号码
         */
        private String poFollowerTelNumber;

        /**
         * 出货小计
         */
        private BigDecimal totalDeliveryAmount;
        /**
         * 退货小计
         */
        private BigDecimal totalReceiveAmount;

        /**
         * 合计
         */
        private BigDecimal totalAmount;

        /**
         * 收款银行
         */
        private String bankName;

        /**
         * 开户行
         */
        private String bankSubbranch;

        /**
         * 账户名称
         */
        private String payee;

        /**
         * 收款账号
         */
        private String bankAccount;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 明细id集合
         */
        @NotEmpty(message = "明细id集合不能为空")
        private List<String> detailIdList;

        /**
         * 对账开始日期
         */
        @NotNull(message = "对账开始日期不能为空")
        private LocalDate startDate;

        /**
         * 对账结束日期
         */
        @NotNull(message = "对账结束日期不能为空")
        private LocalDate endDate;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 明细修改
         */
        @NotEmpty(message = "对账明细不能为空")
        private List<PoReconciliationDetailDTO.UpdateDTO> detailList;
    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ScmUpdateDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 抬头备注
         */
        @NotBlank(message = "抬头备注不能为空")
        private String remark;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 明细修改
         */
        @NotEmpty(message = "对账明细不能为空")
        private List<PoReconciliationDetailDTO.ScmUpdateDTO> detailList;
    }


    /**
     * 对账单导出明细Excel
     */
    @Data
    @NoArgsConstructor
    public static class ExportDetailDTO {
        /**
         * 对账单号
         */
        private String code;
        /**
         * 对账状态
         */
        private String status;
        /**
         * 对账状态名称
         */
        private String statusName;

        /**
         * 对账周期
         */
        private String cycle;

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
         * 单据单号
         */
        private String sourceCode;
        /**
         * 采购单号
         */
        private String poCode;
    }

}