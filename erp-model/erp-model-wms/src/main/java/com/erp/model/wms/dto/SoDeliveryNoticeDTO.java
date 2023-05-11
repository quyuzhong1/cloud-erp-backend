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
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;
        /**
         * 单据编号
         */
        private String code;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 客户id
         */
        private List<String> customerIdList;
        /**
         * 库存组织
         */
        private List<String> inventoryOrgIdList;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 作废状态
         */
        private String invalidStatus;
        /**
         * 销售员id
         */
        private List<String> sellerIdList;
        /**
         * 要货日期
         */
        private List<LocalDate> requireDataList;
        /**
         * 出货仓库
         */
        private List<String> warehouseIdList;
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
        private String invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 发货状态
         */
        private String deliveryStatus;
        /**
         * 发货状态名称
         */
        private String deliveryStatusName;
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
         * 收货人
         */
        private String receiverName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 交货方式 wms/common/enumDropDown?type=deliveryMode
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
         * 主键id
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
         * 收货人
         */
        private String receiverName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 交货方式 wms/common/enumDropDown?type=deliveryMode
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
         * 联系人id
         */
        private String contactsUserId;
        /**
         * 联系人名称
         */
        private String contactsUserName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 收货地址
         */
        private String receiveAddress;
        /**
         * 交货方式 wms/common/enumDropDown?type=deliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        private String deliveryModeDict;
        /**
         * 作废状态
         */
        private String invalidStatus;
        /**
         * 作废描述
         */
        private String invalidRemark;
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
         * 备注
         */
        private String remark;
    }
}
