package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 18:05
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class listDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 申请单号
         */
        private String code;

        /**
         * 单据状态（待提交，审核中，审核不通过，已审核）
         */
        private String approveStatusName;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 采购单关联状态（未生成，部分生成，已生成）
         */
        private String purchaseRelatedType;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 申请数量
         */
        private Integer applyQty;

        /**
         * 实际采购数量
         */
        private Integer realPurchaseQty;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 目的仓库名称
         */
        private String destWarehouseName;

        /**
         * 备注
         */
        private String remark;

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
    public static class searchParamDTO extends SortDTO {
        /**
         * 审核状态
         */
        private List<String> approveStatusList;

        /**
         * 采购订单生成状态（0未生成，1部分生成，2已生成)
         */
        private List<String> createPoTypeList;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 计划交期
         */
        private List<LocalDate> planDeliveryDateList;

        /**
         * 目的仓库id
         */
        private List<String> destWarehouseIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 审核时间开始
         */
        private List<LocalDate> approveTimeList;

        /**
         * 申请人id
         */
        private List<String> applyUserIdList;

        /**
         * 创建人id
         */
        private List<String> createUserIdList;
    }

    @Data
    @NoArgsConstructor
    public static class commonDTO {
        /**
         * 申请日期
         */
        @NotEmpty(message = "申请日期不能为空")
        private LocalDate applyDate;

        /**
         * 申请人id
         */
        private String applyUserId;

        /**
         * 申请人部门id
         */
        private String applyDeptId;

        /**
         * 新品首批（false否,true是）
         */
        @NotEmpty(message = "新品首批不能为空")
        private Boolean isFirstMassProduct;
    }


    @Data
    @NoArgsConstructor
    public static class addDTO extends commonDTO{
        /**
         * 采购申请明细
         */
        @Valid
        private List<PurchaseApplicationDetailDTO.addDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class updateDTO extends commonDTO{
        /**
         * 主表id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 采购申请明细
         */
        @Valid
        private List<PurchaseApplicationDetailDTO.updateDTO> details;

    }

    @Data
    @NoArgsConstructor
    public static class viewDTO extends updateDTO{

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
