package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售出库
 *
 * @author Lambda
 * @Classname SoOutstockDTO

 * @Date 2023-05-11 10:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoOutstockDTO implements Serializable {

    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * 搜索类型
         */
        private String searchType;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 数据合计
     */
    @Data
    @NoArgsConstructor
    public static class PagingTotalDTO {

        /**
         * 实际数量
         */
        private Integer actualTotalQty;

        /**
         * 应发数量
         */
        private Integer planTotalQty;

        /**
         * 价税合计（CNY）
         */
        private BigDecimal totalTaxAmount;
    }

    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * code
         */
        private String code;


        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 来源code
         * 对应发货通知单code
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 审核状态code
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;


        /**
         * 类型
         */
        private String orderType;

        /**
         * 类型名称
         */
        private String orderTypeName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名
         */
        private String invalidStatusName;


        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;


        /**
         * 国家id
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 销售员部门id
         */
        private String salesDeptId;

        /**
         * 销售员部门名
         */
        private String salesDeptName;
        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 出货仓库
         */
        private String warehouseName;


        /**
         * 销售组织名
         */
        private String salesOrgName;

        /**
         * 发货组织
         */
        private String warehouseOrgId;

        /**
         * 发货组织名
         */
        private String warehouseOrgName;


        /**
         * 出库 日期
         */
        private LocalDate actualDeliveryDate;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;

        /**
         * 运输单号
         */
        private String trackNo;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 单位
         */
        private String unit;

        /**
         * 应发数量
         */
        private Integer planQty;

        /**
         * 实发数量
         */
        private Integer actualQty;


        /**
         * 最新审核人
         */
        private String approveUserName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 客户订单号
         */
        private String customerOrderNo;

        /**
         * 销售单价
         */
        private BigDecimal price;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 销售单价（本位币）
         */
        private BigDecimal cnyPrice;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 含税单价（本位币）
         */
        private BigDecimal cnyTaxPrice;

        /**
         * 价税合计（本位币）
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;
    }

    /**·
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         * all 全部
         * waitApprove 待审核
         * approve 已审核
         * reject 审核不通过
         */
        @StateEnumValue(strValues = {"all","waitSubmit", "waitApprove", "approve", "reject"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;

        /**
         * sku no 集合
         */
        private List<String> skuNoList;

        /**
         * code
         */
        private String code;

        /**
         * 销售code
         */
        private String soCode;

        /**
         * 类型
         */
        private String orderType;


        /**
         * 审核列表集合
         */
        private List<String> approveStatusList;

        /**
         * 作废状态
         * true 已作废
         * false 未作废
         */
        private Boolean invalidStatus;


        /**
         * 客户 集合
         */
        private List<String> customerIdList;

        /**
         * 国家 集合
         */
        private List<String> countryIdList;

        /**
         * 销售员 id 集合
         */
        private List<String> sellerIdList;

        /**
         * 销售部门
         */
        private String salesDeptId;

        /**
         * 出库日期
         */
        private List<LocalDate> billDateList;

        /**
         * 出库仓库
         */
        private List<String> warehouseIdList;

        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 运单号
         */
        private List<String> trackNoList;

        /**
         * 客户订单号
         */
        private String customerOrderNo;

    }

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单不能为空")
        private String soId;

        /**
         * 来源id
         */
        private String sourceId;


        /**
         * 来源id
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 仓库id
         */
        @NotBlank(message = "出货仓库不能为空")
        private String warehouseId;

        /**
         * 仓管员
         */
        private String warehouseKeeperId;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;


        /**
         * 客户id
         */
        private String customerId;

        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;

        /**
         * 运输单号
         */
        private String trackNo;

        /**
         * 承运商id 来源供应商
         * http://172.16.100.11:3002/project/83/interface/api/14038    categoryType=logistics
         */
        private String carrierId;

        /**
         * 销售员
         */
        private String sellerId;



        /**
         * 客户订单号
         */
        private String customerOrderNo;

        /**
         * 详情
         */
        @Valid
        @Size(min = 1, message = "销售出库详情不能为空")
        private List<SoOutstockDetailDTO.AddDTO> detailList;


    }


    /**
     * 下推销售出库单列表
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoOutstockViewDTO {
        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单不能为空")
        private String soId;
        /**
         * 来源id
         */
        @NotBlank(message = "来源不能为空")
        private String sourceId;


        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 来源code
         */
        @NotBlank(message = "来源code不能为空")
        private String sourceCode;

        /**
         * 发货组织
         */
        private String deliveryOrgId;

        /**
         * 发货组织
         */
        private String deliveryOrgName;

        /**
         * 订单类型
         */
        private String orderType;

        /**
         * 预计发货时间
         */
        private LocalDate planDeliveryDate;

        /**
         * 承运商
         */
        private String carrierId;

        /**
         * 运输单号
         */
        private String trackNo;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 仓库名
         */
        private String warehouseName;


        @NotBlank(message = "来源明细不能为空")
        private String sourceDetailId;

        /**
         * 销售订单明细id
         */
        @NotBlank(message = "销售订单明细不能为空")
        private String soDetailId;


        @NotBlank(message = "sku不能为空")
        private String skuId;

        @NotBlank(message = "sku no不能为空")
        private String skuNo;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 库位名称
         */
        private String warehouseLocationName;


        /**
         * 箱麦附件名集合
         */
        private List<String> attachNameList;

        /**
         * 箱麦附件url集合
         */
        private List<String> attachUrlList;


        /**
         * 发货数量
         */
        @NotNull(message = "发货数量不能为空")
        @DecimalMin(value = "1", message = "发货数最小值为1")
        @DecimalMax(value = "999999999", message = "发货数最大值为999999999")
        private Integer qty;


        private String remark;

        /**
         * 是否关闭
         */
        private Boolean isClose;

        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 发货数量
         */
        private Integer deliveryQty;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 出库单号
         */
        private String code;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单id
         */
        private String soCode;

        private ApproveStatusEnum approveStatus;


        private String approveStatusName;


        /**
         * 单据类型名
         */
        private String typeName;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 库存组织id
         */
        private String warehouseOrgId;

        /**
         * 库存组织名
         */
        private String warehouseOrgName;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;


        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售部门id
         */
        private String salesDeptName;


        /**
         * 销售组织
         */
        private String salesOrgName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 仓管员
         */
        private String warehouseKeeperId;

        /**
         * 仓管员名称
         */
        private String warehouseKeeperName;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;


        /**
         * 国家id
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;



        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 承运商id 来源供应商
         */
        private String carrierId;

        /**
         * 承运商名称 来源供应商
         */
        private String carrierName;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;

        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;

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
         * 联系地址
         */
        private String receiveAddress;


        /**
         * 交货方式
         */
        private String deliveryModeName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 销售订单备注
         */
        private String soRemark;

        /**
         * 出库日期
         */
        private LocalDate billDate;

        /**
         * 客户订单号
         */
        private String customerOrderNo;

        private List<SoOutstockDetailDTO.ViewDTO> detailList;

    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        @NotBlank(message = "销售出库单不能为空")
        private String id;

        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单不能为空")
        private String soId;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;


        /**
         * 单据类型
         */
        private String orderType;


        /**
         * 仓库id
         */
        @NotBlank(message = "出货仓库不能为空")
        private String warehouseId;

        /**
         * 仓管员
         */
        private String warehouseKeeperId;


        /**
         * 客户id
         */
        private String consumerId;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;

        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;

        /**
         * 运输单号
         */
        private String trackNo;

        /**
         * 承运商id 来源供应商
         */
        private String carrierId;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 联系电话
         */
        private String telNumber;

        /**
         * 联系地址
         */
        private String receiverAddress;

        /**
         * 交货方式
         */
        private String deliveryModeDict;

        /**
         * 出库日期
         */
        @NotNull(message = "出库日期不能为空")
        private LocalDate billDate;



        /**
         * 客户订单号
         */
        private String customerOrderNo;

        /**
         * 详情
         */
        @Size(min = 1, message = "销售出库详情不能为空")
        private List<SoOutstockDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class PagingUpdateDTO {

        /**
         * 主键id
         */
        @NotEmpty(message = "主键id不能为空")
        private List<String> idList;

        /**
         * 运输单号
         */
        private String trackNo;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class SoRefDTO {


        private String id;

        /**
         * 出库单号
         */
        private String code;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 单据类型
         */
        private String orderType;

        /**
         * 单据类型名
         */
        private String orderTypeName;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名
         */
        private String customerName;

        /**
         * 库存组织id
         */
        private String warehouseOrgId;

        /**
         * 库存组织名
         */
        private String warehouseOrgName;

        /**
         * 销售组织
         */
        private String salesOrgName;


        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;


        /**
         * 应发数量
         */
        private Integer planQty;

        /**
         * 实发数量
         */
        private Integer actualQty;

        /**
         * 库存单位
         */
        private String unit;

        /**
         * 出货仓库id
         */
        private String warehouseId;

        /**
         * 出货仓库
         */
        private String warehouseName;


        /**
         * 出库 日期
         */
        private LocalDate outStockDate;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;


        /**
         * 实际发货时间
         */
        private LocalDate actualDeliveryDate;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


    }

    @Data
    @NoArgsConstructor
    public static class ApproveCountDTO {
        /**
         * 类型
         */
        private String approveStatus;

        /**
         * 数量
         */
        private Integer count;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;
    }

    /**
     * 打印
     */
    @Data
    @NoArgsConstructor
    public static class PrintDTO {
        /**
         * 客户
         */
        private String customerName;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 收货地址
         */
        private String receiveAddress;

        /**
         * 联系电话
         */
        private String telNumber;

        /**
         * 合计数量
         */
        private Integer sumNumber;

        /**
         * 打印明细
         */
        private List<PrintDetailDTO> printDetailList;

    }

    /**
     * 打印明细
     */
    @Data
    @NoArgsConstructor
    public static class PrintDetailDTO {
        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * ERP系统sku
         */
        private String productSkuNo;

        /**
         * sku名称
         */
        private String productName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 数量
         */
        private Integer qty;
    }

    /**
     * PDA:分页查询
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingViewDTO {
        /**
         * id
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
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名
         */
        private String approveStatusName;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 仓库名
         */
        private String warehouseName;

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
         * 实发数量
         */
        private Integer actualQty;
    }

    /**
     * PDA:列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingParamDTO extends SortDTO {
        /**
         * 审核状态：根据tab页传审核状态
         */
        private List<String> approveStatusList;

        /**
         * 出库日期
         */
        private List<LocalDate> actualDeliveryDateList;
    }

    /**
     * PDA:列表状态
     * @Author Luo_WG
     * @Date 2023/8/11 9:15
     **/
    @Data
    @NoArgsConstructor
    public static class PdaCountDTO {
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
