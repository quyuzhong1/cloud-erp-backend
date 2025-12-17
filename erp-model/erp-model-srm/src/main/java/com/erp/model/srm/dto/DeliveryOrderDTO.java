package com.erp.model.srm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 送货单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Data
@NoArgsConstructor
public class DeliveryOrderDTO implements Serializable {


    @Data
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class TotalDetail {
        /**
         * 订单数量
         */
        private Integer orderQty;

        /**
         * 送货数量
         */
        private Integer deliveryQty;

        /**
         * 赠品送货数量
         */
        private Integer giftQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 赠品收货数量
         */
        private Integer giftReceiveQty;

        private String id;

        private String detailId;

        private String purchaseId;

    }

    @Data
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class TotalInfo {
        /**
         * 订单数量
         */
        private Integer totalOrderQty;

        /**
         * 送货数量
         */
        private Integer totalDeliveryQty;

        /**
         * 赠品送货数量
         */
        private Integer totalGiftQty;

        /**
         * 收货数量
         */
        private Integer totalReceiveQty;

        /**
         * 赠品收货数量
         */
        private Integer totalGiftReceiveQty;

    }

    @Data
    @NoArgsConstructor
    public static class GenerateDTO {
        List<GenerateReceiveDTO> generateReceiveDTOList;
    }

    /**
     * 下推收货单列表查询DTO
     */
    @Data
    @NoArgsConstructor
    public static class GenerateReceiveDTO {

        /**
         * 收货单id
         */
        @NotBlank(message = "收货单id不能为空")
        private String id;

        /**
         * 收货单明细Id
         */
        @NotBlank(message = "收货单明细Id不能为空")
        private String detailId;

        /**
         * 收货数量
         */
        @NotNull(message = "收货数量不能为空")
        private Integer receiveQty;

        /**
         * 赠品收货数量
         */
        @NotNull(message = "赠品收货数量不能为空")
        private Integer giftReceiveQty;

        private boolean autoSubmit;

        /**
         * 收货人id
         */
        private String receiveUserId;

        /**
         * 收货人部门id
         */
        private String receiveDeptId;

        /**
         * 收货日期
         */
        private LocalDate billDate;

    }

    /**
     * 下推收货单列表查询DTO
     */
    @Data
    @NoArgsConstructor
    public static class GenerateReceiveListDTO {

        /**
         * 收货单id
         */
        private String id;

        /**
         * 收货单明细Id
         */
        private String detailId;

        /**
         * 收货状态
         */
        private String receiptStatus;

        /**
         * 明细收货状态
         */
        private String detailReceiptStatus;

        /**
         * 送货单号
         */
        private String code;

        /**
         * 供应商Id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;

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
         * 送货数量
         */
        private Integer deliveryQty;

        /**
         * 赠品数量
         */
        private Integer giftQty;

    }

    /**
     * 打印DTO
     */
    @Data
    @NoArgsConstructor
    public static class PrintDTO {

        /**
         * id
         */
        private String id;

        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 采购单号
         */
        private String sourceCode;

        /**
         * 送货单号
         */
        private String code;

        /**
         * 预计到达日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 客户名称
         */
        private String customerName;


        /**
         * 收货员名
         */
        private String receiveUserName;

        /**
         * 收货电话
         */
        private String receivePhone;

        /**
         * 收货地址
         */
        private String receiveAddress;

        /**
         * 制单人
         */
        private String createUserName;

        /**
         * 目的仓
         */
        private String toWarehouseName;

        /**
         * 明细
         */
        private List<DeliveryOrderDetailDTO.PrintDTO> detailPrintList;
    }

    @Data
    @NoArgsConstructor
    public static class StatusListDTO {

        /**
         * 收货状态
         */
        private String receiptStatus;

        /**
         * 是否打印
         */
        private Boolean isPrint;

        /**
         * 收发差异
         */
        private Integer qtyDifferences;

        private Integer count;
    }

    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TabListDTO {

        /**
         * 类型 （all全部， waitReceive 待收货， waitReceiveAndPrint 待收货-未打印 waitReceiveAndPrinted 待收货-已打印 received 已收货 qtyDifference 收发差异）
         */
        private String searchType;

        private Integer count;
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 送货单id
         */
        private String id;

        /**
         * 送货单明细id
         */
        private String detailId;

        /**
         * 采购订单明细id
         */
        private String purchaseDetailId;

        /**
         * 送货单号
         */
        private String code;

        /**
         * 预计到达日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 收货单号
         */
        private String receiveCode;

        /**
         * 是否打印
         */
        private Boolean isPrint;

        /**
         * 收货状态
         */
        private String receiptStatus;

        /**
         * 收货状态中文
         */
        private String receiptStatusName;

        /**
         * 收货状态
         */
        private String detailReceiptStatus;

        /**
         * 收货状态中文
         */
        private String detailReceiptStatusName;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 订单单号
         */
        private String sourceId;

        /**
         * 订单单号
         */
        private String sourceCode;
        /**
         * 订单类型
         */
        private String type;
        /**
         * 订单类型名称
         */
        private String typeName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 是否加急
         */
        private Boolean isUrgent;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 订单数量
         */
        private Integer orderQty;

        /**
         * 待交货量
         */
        private Integer deliveryQty;

        /**
         * 赠品数量
         */
        private Integer giftQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 赠品收货数量
         */
        private Integer giftReceiveQty;

        /**
         * 质检合格数
         */
        private Integer qcGoodQty;

        /**
         * 备注明细
         */
        private String remark;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 目的仓
         */
        private String toWarehouseName;

        /**
         * 打印日期
         */
        private LocalDate printDate;

        /**
         * 确认收货日期
         */
        private LocalDate confirmReceiveDate;

        /**
         * 收货员名
         */
        private String receiveUserName;

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
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO extends SortDTO {

        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 供应商IdList
         */
        private List<String> supplierIdList;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 送货单号
         */
        private String code;

        /**
         * 收货状态
         */
        private String receiptStatus;

        /**
         * 收货状态名称
         */
        private String receiptStatusName;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;


        /**
         * 来源id(采购订单id)
         */
        private String sourceId;

        /**
         * 打印日期
         */
        private LocalDate printDate;

        /**
         * 预计到达日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 订单号
         */
        private String sourceCode;

        /**
         * 是否打印
         */
        private Boolean isPrint;

        /**
         * 联系人name
         */
        private String contactName;

        /**
         * 目的仓名称
         */
        private String toWarehouseName;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 联系人id
         */
        private String contactId;

        /**
         * 目的仓id
         */
        private String toWarehouseId;

        /**
         * 收货员名
         */
        private String receiveUserName;
        /**
         * 收货电话
         */
        private String receivePhone;

        /**
         * 收货地址
         */
        private String receiveAddress;

        /**
         * 产品明细
         */
        private List<DeliveryOrderDetailDTO.ViewDTO> detailList;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<DeliveryOrderDetailDTO.AddDTO> detailList;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDeliveryDTO implements Serializable {

        /**
         * 主键id
         */
        @NotNull(message = "采购订单id不能为空")
        private String id;

        /**
         * 采购订单明细id
         */
        @NotNull(message = "采购订单明细id不能为空")
        private String purchaseDetailId;

        /**
         * 采购单号
         */
        private String code;

        /**
         * 供应商id
         */
        private String supplierId;

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
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 采购数量/订单数量[可排序]
         */
        private Integer purchaseQty;
        /**
         * 未交货数量/待交货量
         */
        private Integer deliveryQty;

        /**
         * 预计到货日期
         */
        @NotNull(message = "预计到货日期不能为空")
        private LocalDate expectDeliveryDate;
        /**
         * 送货数量
         */
        @NotNull(message = "送货数量不能为空")
        @Min(value = 1,message = "送货数量最小值为1")
        private Integer planDeliveryQty;

        /**
         * 赠品数量
         */
        @NotNull(message = "赠品数量不能为空")
        @Min(value = 0,message = "赠品数量最小值为0")
        private Integer giftQty;
        /**
         * 备注信息
         */
        private String remark;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 预计到达日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 明细数据
         */
        private List<DeliveryOrderDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19, message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19, message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
         * 预计到达日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 收货单号
         */
        private String receiveCode;

        /**
         * 来源订单号
         */
        @NotBlank(message = "来源订单号不能为空")
        @Size(max = 50, message = "来源订单号最大长度不能超过50位")
        private String sourceCode;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 联系人id
         */
        private String contactId;

        /**
         * 联系人name
         */
        private String contactName;

        /**
         * 目的仓id
         */
        @NotBlank(message = "目的仓id不能为空")
        @Size(max = 19, message = "目的仓id最大长度不能超过19位")
        private String toWarehouseId;

        /**
         * 目的仓名称
         */
        @NotBlank(message = "目的仓名称不能为空")
        @Size(max = 50, message = "目的仓名称最大长度不能超过50位")
        private String toWarehouseName;

        /**
         * 打印日期
         */
        private LocalDate printDate;

        /**
         * 确认收货日期
         */
        private LocalDate confirmReceiveDate;

        /**
         * 收货员id
         */
        private String receiveUserId;

        /**
         * 收货员名
         */
        private String receiveUserName;

        /**
         * 收货电话
         */
        private String receivePhone;

        /**
         * 收货地址
         */
        private String receiveAddress;

        /**
         * 来源类型  {@link DeliveryOrderEnum.SourceTypeEnum}
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 收货状态 {@link DeliveryOrderEnum.ReceiptStatusEnum}
         */
        private String receiptStatus;

        /**
         * 是否打印
         */
        private Boolean isPrint;


    }

    /**
     * SRM 订单确认列表统计
     * ExecutionStatusEnum
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaitDeliveryCountDTO {

        /**
         * 全部  all
         * 已超期  expired
         * 即将超期  almostOverdue
         * 1个月内  inOneMonth
         * 2个月内  inTwoMonth
         * 2个月以后  twoMonthLater
         */
        private String type;
        /**
         * 名称
         */
        private String name;
        /**
         * 数量
         */
        private Integer count;

    }
}