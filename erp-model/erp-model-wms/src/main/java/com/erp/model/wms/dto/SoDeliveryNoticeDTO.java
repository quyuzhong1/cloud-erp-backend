package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SoDeliveryNoticeDTO {

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParam extends SortDTO {
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
         * 发货状态 wms/common/enumDropDown?type=DeliveryStatus
         * "unShipped","未发货"
         * "partialShipment","部分发货"
         * "completeShipment","已发货"
         */
        private String deliveryStatusDict;
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
        private List<LocalDateTime> createTimeList;
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
         * 发货通知单号
         */
        private String code;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 销售单id
         */
        private String sourceId;
        /**
         * 销售单明细id
         */
        private String sourceDetailId;
        /**
         * 客户
         */
        private String customerName;
        /**
         * 发货组织
         */
        private String deliveryOrgName;
        /**
         * 单据状态
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
         * 发货状态 wms/common/enumDropDown?type=DeliveryStatus
         * "unShipped","未发货"
         * "partialShipment","部分发货"
         * "completeShipment","已发货"
         */
        private String deliveryStatusDict;
        /**
         * 发货状态名称
         */
        private String deliveryStatusDictName;
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
         * 来源id（销售单id）
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;
        /**
         * 来源类型（用于下推）：界面新增可传空值
         */
        private String sourceType;
        /**
         * 发货组织
         */
        @NotBlank(message = "发货组织不能为空")
        private String deliveryOrgId;
        /**
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;
        /**
         * 承运商id
         */
        @NotBlank(message = "承运商不能为空")
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
         * 交货方式 wms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        private String deliveryModeDict;
        /**
         * 收货地址
         */
        private String receiveAddress;
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
         * 发货组织
         */
        @NotBlank(message = "发货组织不能为空")
        private String deliveryOrgId;
        /**
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;
        /**
         * 承运商id
         */
        @NotBlank(message = "承运商不能为空")
        private String carrierId;
        /**
         * 运输单号
         */
        private String trackNo;
        /**
         * 出货仓库
         */
        private String WarehouseId;
        /**
         * 收货人
         */
        private String receiverName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 交货方式 wms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        private String deliveryModeDict;
        /**
         * 收货地址
         */
        private String receiveAddress;
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
        private String deptId;
        /**
         * 销售部门名称
         */
        private String deptName;
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
        private String deliveryOrgId;
        /**
         * 发货组织名称
         */
        private String deliveryOrgName;
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
         * 交货方式 wms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        private String deliveryModeDict;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 发货状态
         */
        private String deliveryStatusDict;
        /**
         * 发货状态名称
         */
        private String deliveryStatusDictName;
        /**
         * 作废描述
         */
        private String invalidRemark;
        /**
         * 附件名集合
         */
        private List<String> attachNameList;
        /**
         * 附件url集合
         */
        private List<String> attachUrlList;
        /**
         * 明细信息
         */
        private List<SoDeliveryNoticeDetailDTO.View> detailList;
    }

    /**
     * 下推发货出库单列表集合
     */
    @Data
    @NoArgsConstructor
    public static class ListGenerateSoDeliveryView {
        private List<GenerateSoDeliveryView> list;
    }

    /**
     * 下推发货出库单列表
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoDeliveryView {
        /**
         * id
         */
        private String id;
        /**
         * 销售单id
         */
        private String sourceId;
        /**
         * 销售单详情表id
         */
        private String sourceDetailId;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 客户
         */
        private String customerName;
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
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;
        /**
         * 附件名集合
         */
        private List<String> attachNameList;
        /**
         * 附件url集合
         */
        private List<String> attachUrlList;
        /**
         * 备注
         */
        private String remark;
    }
}
