package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SoDeliveryNoticeDTO {
    private SoDeliveryNoticeDTO() {
        throw new IllegalStateException("Utility SoDeliveryNoticeDTO class");
    }
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
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 客户id
         */
        private List<String> customerIdList;
        /**
         * 通知单单据编号
         */
        private String code;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 销售员id
         */
        private List<String> sellerIdList;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 发货状态 true ：已发货， false：未发货
         */
        private Boolean deliveryStatus;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 要货日期
         */
        private List<LocalDate> requireDateList;
        /**
         * 出货仓库
         */
        private List<String> warehouseIdList;
        /**
         * 创建人id
         */
        private List<String> createUserIdList;
        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;
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
         * 销售单id
         */
        private String sourceId;
        /**
         * 类型
         */
        private String sourceType;
        /**
         * 销售单明细id
         */
        private String sourceDetailId;
        /**
         * 发货通知单号
         */
        private String code;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 客户
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouse_name;
        /**
         * 发货组织id
         */
        private String warehouseOrgId;
        /**
         * 发货组织
         */
        private String warehouseOrgName;
        /**
         * 是否已完成拣货
         */
        private Boolean isPicked;
        /**
         * 是否已完成装箱
         */
        private Boolean isPacked;
        /**
         * 生成拣货单状态
         */
        private String generationPickStatus;
        /**
         * 单据状态
         */
        private String approveStatus;
        /**
         * 单据状态名称
         */
        private String approveStatusName;
        /**
         * 装箱状态
         */
        private String packingStatus;

        /**
         * 装箱状态名称
         */
        private String packingStatusName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 发货状态 true ：已发货， false：未发货
         */
        private Boolean deliveryStatus;
        /**
         * 发货状态名称
         */
        private String deliveryStatusName;
        /**
         * skuId
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
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 拣货数量
         */
        private Integer pickedQty;

        /**
         * 拣货单
         */
        private Integer pickeStatus;
        /**
         * 装箱数量
         */
        private Integer packingQty;
        /**
         * 销售单位
         */
        private String unit;
        /**
         * 销售员
         */
        private String sellerName;
        /**
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;
        /**
         * 完成打包日期
         */
        private LocalDate packDate;
        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;
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
         * 中转仓库集合
         */
        private String transferWarehouseIds;
        /**
         * 中转仓库名称
         */
        private String transferWarehouseNames;
        /**
         * 客户PO号
         */
        private String customerPO;
        /**
         * 目的地
         */
        private String toCountry;

    }

    /**
     * 列表状态数量查询
     */
    @Data
    @NoArgsConstructor
    public static class StatusCountDTO {
        /**
         * 类型(toBeApprove 待审核，unShipped 待发货，reject 不通过，completeShipment 已发货)
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
         * 来源id（退货单id）
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;
        /**
         * 来源类型（用于下推）：界面新增可传空值
         */
        private String sourceType;
        /**
         * 承运商id
         */
        private String carrierId;
        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;
        /**
         * 运输单号
         */
        private String trackNo;
        /**
         * 出货仓库
         */
        private String warehouseId;
        /**
         * 明细信息
         */
        private List<SoDeliveryNoticeDetailDTO.Add> detailList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * 主表表id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 来源id（销售单id）
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;
        /**
         * 承运商id
         */
        private String carrierId;
        /**
         * 运输单号
         */
        private String trackNo;
        /**
         * 出货仓库
         */
        private String warehouseId;
        /**
         * 收货人
         */
        private String receiverName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 交货方式 oms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        private String deliveryModeDict;
        /**
         * 收货地址
         */
        private String receiveAddress;
        /**
         * 中转仓库集合
         */
        private List<String> transferWarehouseIdList;
        /**
         * 明细信息
         */
        private List<SoDeliveryNoticeDetailDTO.Update> detailList;
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
         * 退货单id
         */
        private String sourceId;
        /**
         * 退货单编号
         */
        private String sourceCode;
        /**
         * 类型
         */
        private String sourceType;
        /**
         * 单据编号
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
         * 单据类型
         */
        private String type;
        /**
         * 单据类型名称
         */
        private String typeName;
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
         * 发货组织id
         */
        private String warehouseOrgId;
        /**
         * 发货组织名称
         */
        private String warehouseOrgName;
        /**
         * 出货仓库
         */
        private String warehouseId;
        /**
         * 出货仓库名称
         */
        private String warehouseName;

        /**
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;
        /**
         * 完成打包日期
         */
        private LocalDate packDate;
        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;
        /**
         * 承运商id
         */
        private String carrierId;
        /**
         * 承运商名称
         */
        private String carrierName;
        /**
         * 运输单号
         */
        private String trackNo;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 收货人
         */
        private String receiverName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 收货地址
         */
        private String receiveAddress;
        /**
         * 交货方式 oms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        private String deliveryModeDict;
        /**
         * 交货方式名称
         */
        private String deliveryModeDictName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 发货状态 true ：已发货， false：未发货
         */
        private Boolean deliveryStatus;
        /**
         * 发货状态名称
         */
        private String deliveryStatusName;
        /**
         * 作废描述
         */
        private String invalidRemark;
        /**
         * 中转仓库集合
         */
        private List<String> transferWarehouseIdList;

        /**
         * 明细信息
         */
        private List<SoDeliveryNoticeDetailDTO.View> detailList;
    }

    /**
     * PDA:发货通知单查询
     */
    @Data
    @NoArgsConstructor
    public static class PdaSoDeliveryNotice {
        /**
         * 主键id
         */
        private String id;

        /**
         * 销售订单
         */
        private String code;

        /**
         * 销售订单编号
         */
        private String soCode;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 仓库Id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名
         */
        private String approveStatusName;
    }

    /**
     * PDA:发货通知单查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaSoDeliveryNoticeParam {
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 采购订单号
         */
        private String code;
    }

    @Getter
    @Setter
    public static class PickingViewDTO {
        /**
         * 明细id
         */
        private String detailId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 客户sku
         */
        private String platformSkuNo;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 计划数量
         */
        private Integer planQty;
        /**
         * 已拣数量
         */
        private Integer pickedQuantity;
        /**
         * 未拣数量
         */
        private Integer unpickedQuantity;
    }

    @Getter
    @Setter
    public static class GeneratePickingDTO {

        @NotBlank(message = "要货申请不能为空")
        private String id;

        @Size(min = 1,message = "至少存在一条明细,才可生成拣货单")
        private List<String> detailIds;
    }

    @Data
    @NoArgsConstructor
    public static class PrintSkuLabelDTO {
        private String id;
        private String detailId;
        private String soId;
        /**
         * 销售订单号
         */
        private String soCode;
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
         * 是否组合品
         */
        private Boolean isCombination;
        /**
         * 客户sku
         */
        private String platformSkuNo;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 打印数量
         */
        private Integer printNum;
        /**
         * 是否显示日期
         */
        private Boolean showDate;
        private String labelUrl;
        /**
         * 标签来源类型
         * LabelSourceTypeEnum
         */
        private String labelSourceType;
    }

    @Data
    @NoArgsConstructor
    public static class PrintSkuLabelConfirmDTO {
        //打印尺寸
        private String size;
        /**
         * 打印样式
         */
        @NotBlank(message = "打印样式不能为空")
        private String skuPrintType;
        /**
         * 生产厂名
         */
//        @NotBlank(message = "生产厂名不能为空")
        private String companyName;
        /**
         * 生产地址
         */
//        @NotBlank(message = "生产地址不能为空")
        private String companyAddress;
        /**
         * 打印明细
         */
        private List<PrintSkuLabelDTO> detailList;
    }
}
