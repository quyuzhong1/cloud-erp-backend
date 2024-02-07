package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.validator.AddGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.MachineTypeEnum;
import com.erp.model.wms.enums.WorkTypeEnum;
import lombok.Data;
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
 * @date 2023/5/10 14:03
 */
@Data
@NoArgsConstructor
public class MachineInfoDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 来源id
         */
        private String sourceId;


        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型 {soInfo:b2b销售订单，MB_FBA_DELIVERY：马帮FBA发货单，firstMileDelivery：头程发货单，soReturnInstock:销售退货入库单，transferApplication：调拨申请单}
         */
        private String sourceType;

        /**
         * 加工单号
         */
        private String code;

        /**
         * 事务类型
         */
        private String workType;

        /**
         * 事务类型名称
         */
        private String workTypeName;

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
         * sku编码
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
         * 加工日期
         */
        private LocalDate billDate;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核完成时间
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

    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;


    }

    @Data
    @NoArgsConstructor
    public static class ListStatusCountDTO {

        /**
         * 类型(toBeApprove待审批，approve审核通过，reject不通过)
         */
        private String searchType;

        /**
         * 数量
         */
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 加工日期
         */
        @NotNull(message = "加工日期不能为空")
        private LocalDate billDate;
        /**
         * 事务类型
         */
        @NotBlank(message = "事务类型不能为空")
        @StateEnumValue(clazz = WorkTypeEnum.class, message = "事务类型有误", groups = {AddGroup.class})
        private String workType;
        /**
         * 仓管员id
         */
        private String warehouseKeeperId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        /**
         * 领料人id
         */
        private String receiverId;
        /**
         * 领料组织id
         */
        private String receiveOrgId;
        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        @StateEnumValue(clazz = MachineTypeEnum.class, message = "单据类型有误", groups = {AddGroup.class})
        private String type;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源类型 {soInfo:b2b销售订单，MB_FBA_DELIVERY：马帮FBA发货单，firstMileDelivery：头程发货单，soReturnInstock:销售退货入库单，transferApplication：调拨申请单}
         */
        private String sourceType;

        /**
         * 来源单号
         */
        private String sourceCode;

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<MachineDetailDTO.AddDTO> detailList;
    }


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
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<MachineDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 加工单号
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

        /**
         * 领料组织名称
         */
        private String receiveOrgName;

        /**
         * 库存组织id
         */
        private String inventoryOrgId;

        /**
         * 库存组织名称
         */
        private String inventoryOrgName;

        /**
         * 明细
         */
        private List<MachineDetailDTO.ViewDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class FindInfoBySkuDTO {
        private String skuNo;
        private String skuId;
    }

}
