package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
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

 * @date 2023/5/10 11:27
 */
@Data
@NoArgsConstructor
public class OtherInstockDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 其他入库编号
         */
        private String code;

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
         * 入库日期
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

        /**
         * 第三方单号
         */
        private String thirdCode;


        /**
         * 备注
         */
        private String remark;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
    }

    @Data
    @NoArgsConstructor
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
         * 主键ids
         */
        private List<String> ids;
        /**
         * 搜索类型
         */
        private String  searchType;
        /**
         * 其他入库编号
         */
        private String  code;
        /**
         * sku编码集合
         */
        private List<String>  skuNoList;
        /**
         * 审核状态集合
         */
        private List<String>  approveStatusList;
        /**
         * 作废状态
         */
        private Boolean  invalidStatus;
        /**
         * 库存方向
         */
        private String   inventoryDirection;
        /**
         * 入库日期集合
         */
        private List<LocalDate>  billDateList;
        /**
         * 收货仓库id集合
         */
        private List<String>  warehouseIdList;
        /**
         * 创建人id集合
         */
        private List<String>  createUserIdList;
        /**
         * 创建时间集合
         */
        private List<LocalDate>   createTimeList;

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
         * 入库日期
         */
        @NotNull(message = "入库日期不能为空")
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
         * 领料员id
         */
        private String receiverId;

        /**
         * 收货仓库id
         */
        private String warehouseId;

        /**
         * 部门id
         */
        @NotBlank(message = "部门不能为空")
        private String deptId;

        /**
         * 入库类型
         */
        @NotBlank(message= "入库类型不能为空")
        @StateEnumValue(clazz = InstockTypeEnum.class, message = "入库类型输入值有误")
        private String type;

        /**
         * 类型名称
         */
        private String typeName;


        /**
         * 第三方单号
         */
        private String thirdCode;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;

        /**
         * 备注
         */
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<OtherInstockDetailDTO.AddDTO> detailList;
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
        private List<OtherInstockDetailDTO.UpdateDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 入库单号
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
        private String orgId;

        /**
         * 库存组织名称
         */
        private String orgName;

        /**
         * 部门名称
         */
        private String deptName;

        /**
         * 仓管员
         */
        private String warehouseKeeperName;

        /**
         * 验收员
         */
        private String receiverName;

        /**
         * 收货仓库
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
         * 第三方单号
         */
        private String thirdCode;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;

        /**
         * 备注
         */
        private String remark;

        /**
         * 明细
         */
        private List<OtherInstockDetailDTO.ViewDTO> detailList;
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
        private List<LocalDate>  billDateList;
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
}
