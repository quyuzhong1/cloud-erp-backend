package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 12:04
 */
@Data
@NoArgsConstructor
public class PurchaseChangeDTO implements Serializable {



    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 变更单号
         */
        private String code;

        /**
         * 采购订单号
         */
        private String purchaseOrderCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 审核状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private String approveStatus;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatusName;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 原采购数量
         */
        private Integer oldQty;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 原含税单价
         */
        private BigDecimal oldPrice;

        /**
         * 原含税金额
         */
        private BigDecimal oldAmount;

        /**
         * 新采购数量
         */
        private Integer qty;

        /**
         * 新含税单价
         */
        private BigDecimal price;

        /**
         * 新含税金额
         */
        private BigDecimal amount;

        /**
         * 变更备注
         */
        private String remark;

        /**
         * 变更人名称
         */
        private String changeUserName;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {
        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 供应商id
         */
        private List<String> supplierIdList;

        /**
         * 审核状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private List<String> approveStatusList;

        /**
         * 作废状态
         */
        private String invalidStatus;

        /**
         * 交货仓库id
         */
        private List<String> deliveryWarehouseIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


        /**
         * 审核时间开始
         */
        private List<LocalDate> approveTimeList;

        /**
         * 创建人id
         */
        private List<String> createUserIdList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 采购订单id
         */
        @NotBlank(message = "采购订单id不能为空")
        private String purchaseOrderId;

        /**
         * 变更日期
         */
        @NotNull(message = "变更日期不能为空")
        private LocalDate changeDate;

        /**
         * 变更人id
         */
        private String changeUserId;

        /**
         * 变更部门id
         */
        private String changeDeptId;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 供应商id
         */
        private String supplierId;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 变更明细
         */
        @Valid
        private List<PurchaseChangeDetailDTO.AddDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
         * 主表id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 变更明细
         */
        @Valid
        private List<PurchaseChangeDetailDTO.UpdateDTO> details;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {

        /**
         * 单据编码
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

    }

}
