package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SoReturnInstockDTO {
    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParam extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;
        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;
        /**
         * 单据编号
         */
        private String code;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 是否委外（true是、false否）
         */
        private Boolean isSubContract;

        /**
         * 库存组织
         */
        private List<String> inventoryOrgIdList;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 客户id
         */
        private List<String> customerIdList;
        /**
         * 销售员id
         */
        private List<String> sellerIdList;
        /**
         * 入库日期
         */
        private List<LocalDate> billDateList;
        /**
         * 创建人id
         */
        private List<String> createUserIdList;
    }

    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingView {
        /**
         * id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 退货明细id
         */
        private String soReturnDetailId;
        /**
         * 退货明细id
         */
        private String soReturnId;
        /**
         * 退货入库单号
         */
        private String code;
        /**
         * 退货单号
         */
        private String sourceCode;
        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 第三方单据编号
         */
        private String thirdCode;

        /**
         * 单据类型
         */
        private String type;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 库存组织
         */
        private String inventoryOrgName;
        /**
         * 单据状态编号
         */
        private String approveStatus;
        /**
         * 单据状态名称
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
         * 销售员Id
         */
        private String sellerId;
        /**
         * 销售员
         */
        private String sellerName;
        /**
         * SkuId
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 应退数量
         */
        private Integer mustQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 实退数量
         */
        private Integer realQty;
        /**
         * 入库日期
         */
        private LocalDate billDate;
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

        /**
         * 是否委外（true是、false否）
         */
        private Boolean isSubContract;
        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
    }

    /**
     * 列表状态数量查询
     */
    @Data
    @NoArgsConstructor
    public static class StatusCountDTO {
        /**
         * 类型(waitSubmit 待提交，approveIng 审核中，reject 审核不通过，approve 已审核)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add {
        /**
         * 退货单id
         */
        private String soReturnId;
        /**
         * 退货单编号
         */
        private String soReturnCode;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源编号
         */
        private String sourceCode;
        /**
         * 退货客户id
         */
        private String customerId;
        /**
         * 销售组织
         */
        private String salesOrgId;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 入库日期
         */
        @NotNull(message = "入库日期不能为空")
        private LocalDate billDate;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
        /**
         * 明细信息
         */
        private List<SoReturnInstockDetailDTO.Add> detailList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * 主键id
         */
        @NotBlank(message = "退货入库单不能为空")
        private String id;
        /**
         * 退货单id
         */
        private String soReturnId;
        /**
         * 退货单编号
         */
        private String soReturnCode;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源编号
         */
        private String sourceCode;
        /**
         * 退货客户id
         */
        private String customerId;
        /**
         * 销售组织
         */
        private String salesOrgId;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 入库日期
         */
        @NotNull(message = "入库日期不能为空")
        private LocalDate billDate;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
        /**
         * 明细信息
         */
        private List<SoReturnInstockDetailDTO.Update> detailList;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class View {
        /**
         * id
         */
        private String id;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源编号
         */
        private String sourceCode;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 退货单id
         */
        private String soReturnId;
        /**
         * 退货单编号
         */
        private String soReturnCode;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态
         */
        private String approveStatusName;

        /**
         * 第三方单据编号
         */
        private String thirdCode;

        /**
         * 单据类型
         */
        private String type;
        /**
         * 单据类型名称
         */
        private String typeName;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 销售组织id
         */
        private String salesOrgId;
        /**
         * 销售组织名称
         */
        private String salesOrgName;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;
        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * 退货日期
         */
        private LocalDate billDate;
        /**
         * 库存组织
         */
        private String inventoryOrgId;
        /**
         * 库存组织名称
         */
        private String inventoryOrgName;
        /**
         * 仓管员id
         */
        private String warehouseKeeperId;
        /**
         * 仓管员名称
         */
        private String warehouseKeeperName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态
         */
        private String invalidStatusName;
        /**
         * 作废描述
         */
        private String invalidRemark;
        /**
         * 审核人id
         */
        private String approveUserId;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;
        /**
         * 明细信息
         */
        private List<SoReturnInstockDetailDTO.View> detailList;
    }

    /**
     * 下推退货入库单-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoReturnInstockView {
        /**
         * id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 退货客户id
         */
        private String customerId;
        /**
         * 退货客户
         */
        private String customerName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 应退数量
         */
        private Integer mustQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 实退数量
         */
        private Integer realQty;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 退货类型名称
         */
        private String returnTypeDictName;
        /**
         * 退货原因
         */
        private String  returnReasonDict;
        /**
         * 退货原因名称
         */
        private String  returnReasonDictName;
        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 仓库
         */
        private String warehouseId;
        /**
         * 仓库
         */
        private String warehouseName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 质检状态
         */
        private String qcStatus;
    }

    /**
     * PDA:分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingView {
        /**
         * 主键id
         */
        private String id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 单据来源
         */
        private String sourceType;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 仓库名称
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

    /**
     * PDA:商品信息
     */
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
         * 实退数量
         */
        private Integer realQty;
    }

    /**
     * PDA:分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingParam extends SortDTO {
        /**
         * 主键id
         */
        private List<String> ids;
        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 入库日期
         */
        private List<LocalDate> billDateList;
    }

    /**
     * PDA:列表状态
     * @Author Luo_WG
     * @Date 2023/8/11 9:15
     **/
    @Data
    @NoArgsConstructor
    public static class PdaSoReturnInstockCountDTO {
        /**
         * 类型(waitSubmitAndReject 待提交/审核不通过，approveIng 审核中，approve 已审核)
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 下推加工单显示
     */
    @Data
    @NoArgsConstructor
    public static class ViewGenerateMachineInfoDTO {
        /**
         * id
         */
        private String id;
        /**
         * 退货入库单据编号
         */
        private String code;
        /**
         * 事务类型
         */
        private String workType;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 仓库id
         */
        private String warehouseId;


        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 仓位
         */
        private String warehouseLocation;
        /**
         * 仓位名称
         */
        private String warehouseLocationName;
        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 拆卸数量
         */
        private Integer qty;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;

        /**
         * 子级skuId
         */
        private String childSkuId;
        /**
         * 子级sku编号
         */
        private String childSkuNo;

        /**
         * 用量
         */
        private Integer quantity;
        /**
         * 子件数量
         */
        private Integer childQty;

        /**
         * 处理类型
         */
        private String handleType;

        /**
         * 子级SKU仓库id
         */
        private String childWarehouseId;
        /**
         * 子级SKU供应商id
         */
        private String childSupplierId;
        /**
         * 子级SKU仓位
         */
        private String childWarehouseLocation;
        /**
         * 子级SKU仓位名称
         */
        private String childWarehouseLocationName;
        /**
         * 子级SKU数量(前端需要的标识)
         */
        private Integer childLength;
        /**
         * 子级SKU是否显示(前端需要的标识)
         */
        private Boolean childHidden;
    }

    @Data
    @NoArgsConstructor
    public static class GenerateMachineInfoDTO {
        /**
         * id
         */
        @NotBlank(message = "id不能为空")
        private String id;
        /**
         * 退货入库单号
         */
        @NotBlank(message = "退货入库单号不能为空")
        private String code;
        /**
         * skuId
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer qty;
        /**
         * bom版本
         */
        @NotBlank(message = "BOM版本不能为空")
        private String bomVersion;
        /**
         * 子级skuId
         */
        @NotBlank(message = "子级SKU不能为空")
        private String childSkuId;
        /**
         * 处理类型 （machineHandleType） http://172.16.100.11:3002/project/92/interface/api/13147
         */
        @NotBlank(message = "子级SKU处理类型不能为空")
        private String handleType;
        /**
         * 子级SKU仓库id
         */
        private String childWarehouseId;
        /**
         * 子级SKU供应商id
         */
        private String childSupplierId;
        /**
         * 子级SKU仓位
         */
        private String childWarehouseLocation;

    }
}
