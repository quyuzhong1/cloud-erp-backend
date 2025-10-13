package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/10 11:27
 */
@Data
@NoArgsConstructor
public class OtherOutstockDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 其他出库编号
         */
        private String code;

        /**
         * 客户
         */
        private String customerName;

        /**
         * 业务类型 sys/dictKingdee/drop/down?typeName=其他出库单业务类型
         */
        private String type;

        /**
         * 业务类型名称
         */
        private String typeName;

        /**
         * 出库类型 sys/dictKingdee/drop/down?typeName=出库类型
         */
        private String outType;
        /**
         * 出库类型名称
         */
        private String outTypeName;

        /**
         * 库存方向
         */
        private String inventoryDirection;

        /**
         * 库存方向名称
         */
        private String inventoryDirectionName;

        /**
         * 状态
         */
        private String approveStatus;

        /**
         * 状态名称
         */
        private String approveStatusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 出库日期
         */
        private LocalDate billDate;

        /**
         * 实收数量
         */
        private Integer actualQty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 流程申请单号
         */
        private String processApplyCode;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 明细备注
         */
        private String detailRemark;

        

    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class SearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         * tabFlag,(toBeApprove待审批，approve已审核，reject不通过)
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 审核状态集合
         */
        private List<String>  approveStatusList;
        /**
         * 作废状态
         */
        private Boolean  invalidStatus;
        /**
         * 入库日期集合
         */
        private List<LocalDate>  billDateList;
    }

    @Data
    @NoArgsConstructor
    public static class ListStatusCountDTO {

        /**
         * 类型(toBeApprove待审批，approve审核通过，reject不通过)
         */
        private String tabFlag;
        /**
         * tab名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 出库日期
         */
        @NotNull(message = "出库日期不能为空")
        private LocalDate billDate;

        /**
         * 库存方向
         */
        @NotBlank(message = "库存方向不能为空")
        @StateEnumValue(clazz = InventoryDirectionEnum.class, message = "库存方向输入值有误")
        private String inventoryDirection;

        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 领料人id
         */
        private String receiverId;

        /**
         * 发货仓库id
         */
        @NotBlank(message = "发货仓库不能为空")
        private String warehouseId;

        /**
         * 领料组织id
         */
        @NotBlank(message = "领料组织不能为空")
        private String receiveOrgId;

        /**
         * 业务类型 sys/dictKingdee/drop/down?typeName=其他出库单业务类型
         */
        @NotBlank(message = "业务类型不能为空")
        private String type;

        private String typeName;

        /**
         * 出库类型 sys/dictKingdee/drop/down?typeName=出库类型
         */
        @NotBlank(message = "出库类型不能为空")
        private String outType;

        private String outTypeName;

        /**
         * 领料部门id
         */
        @NotBlank(message = "领料部门不能为空")
        private String deptId;

        /**
         * 流程申请单号
         */
        private String processApplyCode;

        /**
         * 备注(同步金蝶)
         */
        private String remark;

        private String sourceCode;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class AddDTO extends CommonDTO {


        /**
         * 客户信息
         */
        @Valid
        private OtherOutstockCustomerDTO.AddDTO otherOutstockCustomer;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<OtherOutstockDetailDTO.AddDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 客户信息
         */
        private OtherOutstockCustomerDTO.UpdateDTO otherOutstockCustomer;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<OtherOutstockDetailDTO.UpdateDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 其他出库编号
         */
        private String  code;

        /**
         * 审核状态
         */
        private String  approveStatus;

        /**
         * 审核状态名称
         */
        private String  approveStatusName;

        /**
         * 库存方向名称
         */
        private String  inventoryDirectionName;

        /**
         * 库存组织id
         */
        private String inventoryOrgId;

        /**
         * 库存组织名称
         */
        private String inventoryOrgName;

        /**
         * 领料组织id
         */
        private String receiveOrgId;

        /**
         * 领料组织名称
         */
        private String receiveOrgName;

        /**
         * 领料员
         */
        private String receiverName;

        /**
         * 仓管员
         */
        private String warehouseKeeperName;

        /**
         * 部门
         */
        private String deptName;

        /**
         * 出货仓库
         */
        private String warehouseName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 备注(同步金蝶)
         */
        private String remark;

        /**
         * 客户信息
         */
        private OtherOutstockCustomerDTO.UpdateDTO otherOutstockCustomer;

        /**
         * 明细
         */
        private List<OtherOutstockDetailDTO.ViewDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class PdaListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 编号
         */
        public String code;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 产品数量
         */
        private Integer detailCount;

        /**
         * 产品信息
         */
        private List<PdaItemDTO> itemList;
    }

    @Data
    @NoArgsConstructor
    public static class PdaItemDTO {
        /**
         * 明细id
         */
        private String id;
        /**
         * sku
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 实收数量
         */
        private Integer actualQty;
    }

    @Data
    @NoArgsConstructor
    public static class PdaSearchParamDTO extends SortDTO {
        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;

        /**
         * 入库日期集合
         */
        private List<LocalDate> billDateList;
    }

    @Data
    @NoArgsConstructor
    public static class PdaListStatusCountDTO {
        /**
         * 类型(waitSubmitAndReject 待提交/审核不通过，approveIng 审核中，approve 已审核)
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateApprovalStatusDTO {
        private OtherOutstockEntity otherOutstockEntity;
         private String approveStatus;
    }
}
