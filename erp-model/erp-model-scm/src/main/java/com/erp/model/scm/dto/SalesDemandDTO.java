package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 17:23
 */
@Data
@NoArgsConstructor
public class SalesDemandDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 备货编号
         */
        private String code;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 流程id
         */
        private String processId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 计划交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 计划备货数量
         */
        private Integer planStockQty;

        /**
         * 目的仓库名称
         */
        private String destWarehouseName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 备货原因
         */
        private String stockReason;

        /**
         * 审核状态编码
         */
        private String approveStatus;

        /**
         * 审核状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private String approveStatusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private String invalidStatusName;

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
         * 主键ids
         */
        private List<String> ids;

        /**
         * 审核状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private List<String> approveStatusList;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 目的仓库id
         */
        private List<String> destWarehouseIdList;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 创建人
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 计划交期开始
         */
        private List<LocalDate> planDeliveryDateList;

        /**
         * 审核时间开始
         */
        private List<LocalDate> approvePassTimeList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 申请日期
         */
        @NotNull(message = "申请日期不能为空")
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
        @NotNull(message = "新品首批不能为空")
        private Boolean isFirstMassProduct;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 备货原因
         */
        @Size(max = 255,message = "备货原因不能大于255字符")
        private String remark;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

    }


    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 变更明细
         */
        @Valid
        @NotEmpty(message = "备货申请明细不能为空")
        private List<SalesDemandDetailDTO.AddDTO> details;
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
        @NotEmpty(message = "备货申请明细不能为空")
        private List<SalesDemandDetailDTO.UpdateDTO> details;
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


    @Data
    @NoArgsConstructor
    public static class GenerateSalesDemandDTO {

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * 来源单号
         */
        @NotBlank(message = "来源编码不能为空")
        private String sourceCode;

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细id不能为空")
        private String sourceDetailId;

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @StateEnumValue(clazz = SourceTypeEnum.class, message = "来源类型有误")
        private String sourceType;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * sku编号
         */
        @NotBlank(message = "sku编号不能为空")
        private String skuNo;

        /**
         * 申请日期
         */
        @NotNull(message = "申请日期不能为空")
        private LocalDate applyDate;

        /**
         * 新品首批不能为空
         */
        @NotNull(message = "新品首批不能为空")
        private Boolean isFirstMassProduct;

        /**
         * 销售订单数量
         */
        @NotNull(message = "销售订单数量不能为空")
        @Min(value = 1,message = "销售订单数量最小值为1")
        @Max(value = 99999999,message = "销售订单数量最大值为99999999")
        private Integer qty;

        /**
         * 备货数量
         */
        @NotNull(message = "备货数量不能为空")
        @Min(value = 1,message = "备货数量最小值为1")
        @Max(value = 99999999,message = "备货数量最大值为99999999")
        private Integer planStockQty;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String remark;
    }
}
