package com.erp.model.wms.dto;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    @AllArgsConstructor
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
        private String totalTaxAmount;
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
         * 平台订单号
         */
        private String platformCode;
        /**
         * 第三方单据编号
         */
        private String thirdCode;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;

        /**
         * 是否拦截
         */
        private Boolean isIntercept;

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
        private LocalDateTime actualDeliveryDate;


        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 打包日期
         */
        private LocalDate packDate;
        /**
         * 运输单号-数据库
         */
        private String trackNos;
        /**
         * 运输单号集合
         */
        private List<String> trackNo;


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

        /**
         * 装箱状态
         */
        private String packingStatus;

        /**
         * 装箱状态中文
         */
        private String packingStatusName;

        /**
         * 物流渠道
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 销售平台
         */
        private String dictPlatform;
        /**
         * 销售平台名称
         */
        private String dictPlatformName;

        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;

        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 订单备注
         */
        private String remark;
        /**
         * 明细备注
         */
        private String detailRemark;
        /**
         * 客户备注
         */
        private String customerRemark;
        /**
         * 客户采购订单号
         */
        private String customerPO;
        /**
         * 分区ID
         */
        private String partitionId;

        /**
         * 军区编码
         */
        private String partitionCode;

        /**
         * 军区名称
         */
        private String partitionName;
    }

    /**·
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        private Boolean invalidStatus;

        private List<String> approveStatusList;

        private List<LocalDate> billDateList;
        
        /**
         * 动态数据源
         */
        private String dynamicDataSource;
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
        private LocalDateTime actualDeliveryDate;

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
         * 批次号，发货单下推时生成
         */
        private String batchNo;
        /**
         * 出库日期
         */
        private LocalDate billDate;


        /**
         * 订单标签
         */
        private String tradeLabel;

        /**
         * 详情
         */
        @Valid
        @Size(min = 1, message = "销售出库详情不能为空")
        private List<SoOutstockDetailDTO.AddDTO> detailList;
        public void buildAddDTO(SoDeliveryNoticeEntity entity) {
            this.soId = entity.getSourceId();
            this.sourceId = entity.getId();
            this.sourceCode = entity.getCode();
            this.sourceType = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
            this.planDeliveryDate = entity.getPlanDeliveryDate();
            if (ObjectUtil.isNotEmpty(entity.getActualDeliveryDate())) {
                this.actualDeliveryDate = entity.getActualDeliveryDate().atStartOfDay();
            }
            this.trackNo = entity.getTrackNo();
            this.carrierId = entity.getCarrierId();
            this.sellerId = entity.getSellerId();
            this.customerId = entity.getCustomerId();
            this.billDate = entity.getActualDeliveryDate();
        }
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
         * 单据日期
         */
        private LocalDate billDate;

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
         * 第三方单据编号
         */
        private String thirdCode;
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
        private LocalDateTime actualDeliveryDate;

        /**
         * 运输单号列表
         */
        private String trackNos;
        /**
         * 运输单号列表
         */
        private List<String> trackNoList;


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

        /**
         * 物流渠道
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 订单标签
         */
        private String tradeLabel;
        /**
         * 订单备注
         */
        private String remark;
        /**
         * 客户备注
         */
        private String customerRemark;

        /**
         * 军区id
         */
        private String partitionId;

        /**
         * 军区编码
         */
        private String partitionCode;

        /**
         * 军区名称
         */
        private String partitionName;

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
        private LocalDateTime actualDeliveryDate;

        /**
         * 运输单号
         */
        private List<String> trackNoList;

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
        @Valid
        @Size(min = 1, message = "销售出库详情不能为空")
        private List<SoOutstockDetailDTO.UpdateDTO> detailList;

        /**
         * 订单标签
         */
        private String tradeLabel;

    }

    @Data
    @NoArgsConstructor
    public static class PagingUpdateDTO {

        /**
         * 主键id
         */
        @NotEmpty(message = "主键id不能为空")
        private String id;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 跟踪号
         */
        private List<String> trackNoList;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
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
        private LocalDateTime outStockDate;


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
        private LocalDateTime actualDeliveryDate;


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
        private List<LocalDateTime> actualDeliveryDateList;
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

    /**
     * 生成销售出库单参数
     */
    @Data
    @NoArgsConstructor
    public static class GenerateB2cDTO {

        /**
         * 批次号
         */
        private String batchNo;

        /**
         * 销售订单id
         */
        private String soId;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 销售订单code
         */
        private String soCode;
        private String dictPlatform;

        /**
         * 订单类型
         */
        private String orderType;

        /**
         * 库存组织
         */
        private String warehouseOrgId;

        /**
         * 库存组织名称
         */
        private String warehouseOrgName;

        /**
         * 承运商
         */
        private String carrierId;

        /**
         * 物流渠道
         */
        private String logisticsChannelId;
        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;


        private LocalDate billDate;

        /**
         * 销售组织id
         */
        private String salesOrgId;


        /**
         * 销售组织名
         */
        private String salesOrgName;

        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 实际发货日期
         */
        private LocalDateTime actualDeliveryDate;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源code
         */
        private String sourceCode;
        /**
         * 第三方编号
         */
        private String thirdCode;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 客户id
         */
        private String customerId;


        /**
         * 客户名
         */
        private String customerName;

        /**
         *
         * 物流轨迹号
         */
        private String trackNo;

        /**
         *
         * 物流运输单号
         */
        private String transportNo;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 国家
         */
        private String country;



        /**
         * 是否是平台仓订单
         */
        private boolean hasPlatformWarehouseOrder = false;

        /**
         * 明细
         */
        private LinkedList<SoOutstockDetailDTO.AddDTO> detailList;


        /**
         * 是否检查sku历史映射
         */
        private boolean checkSkuHistory = true;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryParamDTO {

        /**
         * 仓库组织id
         */
        private String orgId;

        /**
         * 仓库组织名称
         */
        private String orgName;

        /**
         * 收货仓库id
         */
        private String warehouseId;

        /**
         * 收货仓库名称
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
         * 数量
         */
        private Integer qty;
    }

    /**
     * 分组汇总sku
     */
    @Data
    @NoArgsConstructor
    public static class GroupSkuDTO {
        /**
         * 发货单id
         */
        private String id;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 产品产品名称
         */
        private String productName;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 待装箱数量
         */
        private Integer waitPackQty;

        /**
         * 装箱数量
         */
        private Integer packQty;
    }

    /**·
     *
     */
    @Data
    @NoArgsConstructor
    public static class ListAmountParamDTO{
        /**
         * skuId集合
         */
        private List<String> skuIds;
        /**
         * 退货订单创建时间
         */
        private String returnCreateDate;
        /**
         * 退货订单的客户（店铺）
         */
        private String customerId;
        /**
         * 币种
         */
        private String currency;

    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class AmountDTO {

        /**
         * 出库单详情id
         */
        private String soOutstockDetailId;

        /**
         * 销售订单详情id
         */
        private String soDetailId;

        /**
         *
         */
        private String skuId;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 销售金额
         */
        private BigDecimal amount;
        /**
         *含税销售金额
         */
        private BigDecimal taxAmount;
        /**
         *汇率
         */
        private BigDecimal exchangeRate;

        /**
         *币种
         */
        private String currency;

        /**
         *币种符号
         */
        private String currencySymbol;
    }

    /**
     * sku最后一次出库日期
     */
    @Data
    @NoArgsConstructor
    public static class LastBillDateDTO {

        private String skuId;


        private LocalDate billDate;
    }
}
