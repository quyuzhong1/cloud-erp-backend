package com.erp.model.oms.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cCategoryTypeEnum;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * B2C销售订单表请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Data
@NoArgsConstructor
public class SoB2cDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadFileDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        private MultipartFile file;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateSoB2cReturnViewDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        private String code;
        /**
         * 明细ID
         */
        @NotBlank(message = "明细ID不能为空")
        private String detailId;
        private String billStatus;

        @NotBlank(message = "skuId不能为空")
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
        private Integer saleQty;

        /**
         * 出库数量
         */
        private Integer outQty;

        /**
         * 已退数量
         */
        private Integer alreadyReturnQty;

        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        private Integer returnQty;

        /**
         * 退货原因 {{oms_url}}common/enumDropDown?type=SoB2cReturnReason
         */
        @NotBlank(message = "退货原因不能为空")
        private String returnReason;

        /**
         * 备注
         */
        private String remark;
        /**
         * 平台订单号
         */
        private String platformOrderNo;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 订单金额
         */
        private BigDecimal amount;

        /**
         * 币别（原币）
         */
        private String currency;

        /**
         * 平台sku
         */
        private String platformSkuNo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryWithNotOutboundDTO {

        /**
         * 表 ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;
        /**
         * 实际发货仓库
         */
        @NotEmpty(message = "实际发货仓库不能为空")
        private String warehouseId;

        /**
         * 平台是否标发
         */
        @NotNull(message = "平台是否标发标识不能为空")
        private Boolean platformShipFlag;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MergeTransferDTO {

        /**
         * 物流商id
         */
        private String logisticSupplierId;
        /**
         * 物流商id
         */
        private String logisticSupplierName;

        /**
         * 中转物流商id
         */
        private String transferLogisticsSupplierId;

        /**
         * 中转渠道id
         */
        private String transferChannelId;

        /**
         * 销售订单
         */
        private List<SoB2cEntity> soB2cEntityList;
    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 （all全部，payment待付款，pending待处理，approveIng审核中，inDistribution配货中，waitShipped代发货，shipped已发货，frozen冻结中，invalid已作废,orderError 异常订单 ）
         */
        private String tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 销售订单的预报统计
     */
    @Data
    @NoArgsConstructor
    public static class ForecastCountDTO  {

        /**
         * 状态
         */
        private String status;


        /**
         * 状态名
         */
        private String statusName;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 分页列表查询参数
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
        private Map<String, String> sqlMap;
        /**
         * 是否全托管
         */
        private Boolean isFullyManaged;
        
        /**
         * 动态数据源
         */
        private String dynamicDataSource;
    }

    @Data
    @NoArgsConstructor
    public static class ExportParamDTO extends  PagingParamDTO{

        /**
         * 导出类型,parentExport(销售套装BOM按父件SKU导出),childExport(销售套装BOM按子件SKU导出)
         * 字典，/wms/dict/drop/down?type=soB2cExportType
         */
        @NotBlank(message = "导出类型不能为空")
        private String exportType;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * vat发票状态
         */
        private String vatInvoiceStatus;
        /**
         * vat发票状态名称
         */
        private String vatInvoiceStatusName;
        /**
         * 平台订单状态
         */
        private String platformOrderStatus;
        /**
         * 平台订单状态名称
         * 全托管平台订单状态
         */
        private String platformOrderStatusName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 订单数量
         */
        private Integer orderQty;

        /**
         * 发票状态，SoB2cNfeStatusEnum枚举,pending待开票,invoicing开票中,invoiceFailure开票失败,notNeedInvoice无需开票,waitUpload待上传,uploadFailure上传失败,uploadSuccess已上传notNeedUpload无需上传
         */
        private String nfeInvoiceStatus;
        /**
         * nfe发票状态名称
         */
        private String nfeInvoiceStatusName;
        /**
         * 单据编码
         */
        private String code;

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

        /**
         * 销售平台
         */
        private String dictPlatform;

        /**
         * 卖家订单编号
         */
        private String sellerOrderCode;

        /**
         * 平台订单号
         */
        private String platformCode;

        /**
         * 店铺
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 付款状态
         */
        private String payStatus;

        /**
         * 单据状态
         */
        private String billStatus;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;
        /**
         * 作废备注
         */
        private String invalidRemark;
        /**
         * 作废类型
         */
        private String invalidType;
        /**
         * 作废类型名称
         */
        private String invalidTypeName;

        /**
         * 组包状态  not 不需要  wait 待组包   already 已经组包
         *
         */
        private String packageStatus;
        /**
         * 中转状态 not 不需要  wait 待中转   already 已经中转
         */
        private String transferStatus;


        /**
         * 国家
         */
        private String countryName;

        /**
         * 国家代号
         */
        private String country;
        /**
         * 省份编码
         */
        private String province;
        /**
         * 省份名称
         */
        private String provinceName;
        /**
         * 城市编码
         */
        private String city;
        /**
         * 城市名称
         */
        private String cityName;
        /**
         * 邮编
         */
        private String postCode;

        /**
         * 是否对接了第三方海外仓
         * true 是
         */
        private Boolean isOverseasProviderWarehouse;

        /**
         * 买家自选物流
         */
        private String sellerLogisticName;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名
         */
        private String logisticsChannelName;

        /**
         * 中转物流商id
         */
        private String transferLogisticsSupplierId;

        /**
         * 中转物流商名
         */
        private String transferLogisticsSupplierName;
        /**
         * 中转物流商渠道id
         */
        private String transferLogisticsChannelId;

        /**
         * 中转物流商渠道名
         */
        private String transferLogisticsChannelName;

        /**
         * 实际运费(优先实际、没有取预估)
         */
        private BigDecimal shippingCost;

        /**
         * 实际运费币别
         */
        private String shippingCostCurrency;

        /**
         * 总利润
         */
        private BigDecimal totalProfit;

        /**
         * 利润币别（列表默认人民币）
         */
        private String profitCurrency;

        /**
         * 利润率
         */
        private BigDecimal profitRate;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 订单状态（审核状态、订单状态，取最后一级状态）
         */
        private String status;

        /**
         * 订单状态名称
         */
        private String statusName;
        /**
         * 买家id
         */
        private String buyerId;

        /**
         * 买家名称
         */
        private String buyerName;

        /**
         * 买家邮箱
         */
        private String email;

        /**
         * 买家电话
         */
        private String telNumber;

        /**
         * 运单号
         */
        private String logisticsCode;

        /**
         * 跟踪号
         */
        private String trackCode;

        /**
         * 订单金额
         */
        private BigDecimal amount;

        /**
         * 币别（原币）
         */
        private String currency;

        /**
         * 付款时间
         */
        private LocalDateTime payTime;

        /**
         * 买家备注
         */
        private String buyerRemark;

        /**
         * 订单备注
         */
        private String remark;

        /**
         * 拦截状态
         */
        private Boolean isIntercept;

        /**
         * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
         */
        private String abnormalType;

        /**
         * 异常原因名称
         */
        private String abnormalTypeName;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源类型，（selfAdd,ERP新增；soB2c，平台新增）
         */
        private String sourceType;
        /**
         * 来源编码
         */
        private String sourceCode;

        /**
         * 标签
         */
        private String label;
        /**
         * 扩展字段
         */
        private String extendData;
        /**
         * 是否可送
         */
        private Boolean isDeliver;
        /**
         * 标签对象
         */
        private LabelDTO labelDTO;

        /**
         * 是否匹配订单规则
         */
        private Boolean isMatchOrderRule;

        /**
         * 是否匹配物流规则
         */
        private Boolean isMatchLogisticsRule;

        /**
         * 订单异常的标示
         */
        private String signOrderError;

        /**
         * 物流类型
         */
        private String logisticType;

        /**
         * 物流类型中文
         */
        private String logisticTypeName;


        /**
         * b2c销售订单明细信息
         */
        private List<SoB2cDetailDTO.ListDTO> detailList;
        /**
         * 运输状态
         */
        private String  trackStatus;

        /**
         * 运输状态
         */
        private String  trackStatusName;

        /**
         * 包装重量
         */
        private BigDecimal weight;

        /**
         * 包装重量单位
         */
        private String weightUnit;
        /**
         * 包装长度单位
         */
        private String sizeUnit;
        /**
         * 包装 长
         */
        private BigDecimal length;
        /**
         * 包装 宽
         */
        private BigDecimal width;
        /**
         * 包装 高
         */
        private BigDecimal height;
        /**
         * 发货仓库id
         */
        private String fromWarehouseId;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 审核人id
         */
        private String approveUserId;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 是否匹配仓库规则
         */
        private Boolean isMatchWarehouseRule;
        /**
         * 冻结类型（manual手动冻结，automatic自动冻结）
         */
        private String frozenType;
        /**
         * 是否地址修改 true 是  false 否
         */
        private Boolean isChangeReceiverAddress;
        /**
         * 是否更换sku true 是  false 否
         */
        private Boolean isChangeSku;
        /**
         * 是否标记不出库发货 true 是  false 否
         */
        private Boolean isNotOutbound;
        /**
         * 是否标记手动发货true 是  false 否(以label为准)
         */
        private Boolean isManualDelivery;
        /**
         * 是否预估运费超限，是：true  否：false
         */
        private Boolean isOverEstimatedShipCost;
        /**
         * 标签
         */
        private Boolean tag;

        /**
         * 提交发货时间
         */
        private LocalDateTime createDeliveryTime;
        /**
         * 实际发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 面单打印时间
         */
        private LocalDateTime finishPrintTime;

        /**
         * 是否超出范围派送
         */
        private Boolean isOutOfRangeDelivery;
        /**
         * ioss税号
         */
        private String iossTaxNo;

        /**
         * 提交发货是否选择渠道
         */
        private Boolean isSelectChannel = false;

        //属性字段

        private String extendId;
        /**
         * 要求发货时间
         */
        private LocalDateTime requiredDeliveryTime;
        /**
         * 要求收货时间
         */
        private LocalDateTime requiredReceiveTime;
        /**
         * 发货预警时间
         */
        private LocalDateTime deliveryWarningTime;
        /**
         * 预警时间
         * 未发货时
         * 当前时间< 预警时间时 无异常 黑色
         * 当前时间> 预警时间时 且 当前时间< 要求发货时间 有异常
         * 要求发货时间-当前时间  正数 橙色  负数红色
         * 已发货时
         * 要求发货时间>实际发货时间：则显示未超时
         * 要求发货时间<实际发货时间：则显示已超期N小时
         */
        private BigDecimal warningHour;
        /**
         * 发货预警描述【导出使用】
         */
        private String deliveryWarningDesc;
        /**
         * 订单来源类型
         * SoB2cExtendOrderSourceTypeEnum
         */
        private String orderSourceType;
        private String orderSourceTypeName;

        /**
         * 送货数量
         */
        private Integer deliveryQty;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 上架数量
         */
        private Integer instockQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
    }

    @Data
    @NoArgsConstructor
    public static class LabelJsonDTO {
        /**
         * 速卖通状态
         */
        private String aliexpressStatus;
        /**
         * 亚马逊状态
         */
        private String amazonStatus;
        /**
         * 亚马逊订单
         */
        private String fulfillmentChannel;
        /**
         * WFS（沃尔玛订单shipNodeType=WFSFulfilled或3PLFulfilled）
         */
        private String shipNodeType;
        /**
         * TikTok状态
         */
        private String tikTokStatus;
        /**
         * 是否退款: true=退款, false=未退款
         */
        private Boolean isRefunded;
        /**
         * 物流类型
         */
        private String logisticType;
        /**
         * 发货类型
         */
        private String deliveryType;
        /**
         * 紧急程度
         * 紧急(最高)-URGENT
         * 加急（高）-EXPEDITED
         * 普通-GENERAL
         */
        private String priorityLevel;
        /**
         * 是否可送
         */
        private Boolean isDeliver;
    }

    @Data
    @NoArgsConstructor
    public static class LabelDTO {

        /**
         * 速卖通状态（RISK_CONTROL，IN_FROZEN）时冻结中
         */
        private String aliexpressStatus;
        /**
         * 亚马逊状态（Unfulfillable）冻结中
         */
        private String amazonStatus;
        /**
         * 组合产品（映射SKU为组合产品）
         */
        private Boolean isCombination;
        /**
         * FBA（亚马逊订单fulfillmentChannel=AFN-亚马逊配送时）
         */
        private String fulfillmentChannel;
        /**
         * 手工订单（在ERP手动创建的订单）
         */
        private Boolean isManual;


        /**
         * 拦截订单（ERP发货拦截中，拦截成功，拦截失败的订单）
         */
        private Boolean isIntercept;
        /**
         * 1、拆分生成的子订单 split
         * 2、合并生成的新订单 merge
         */
        private String refType;

        /**
         * 合并数量
         */
        private Integer mergeCount;

        /**
         * WFS（沃尔玛订单shipNodeType=WFSFulfilled或3PLFulfilled）
         */
        private String shipNodeType;

        private String mode;
        /**
         * 美客多（mode=me2 且 logistic_type = fulfillment是官方仓发货）
         */
        private String logisticType;

        /**
         * 是否平台仓订单 true 是 fasle 不是
         */
        private Boolean isPlatformWarehouseOrder;

        /**
         * TikTok状态
         */
        private String tikTokStatus;

        /**
         * 是否退款: true=退款, false=未退款
         */
        private Boolean isRefunded;
        /**
         * 发货类型
         * NORMAL-普通备货
         * JIT-JIT备货
         */
        private String deliveryType;
        /**
         * 紧急程度
         * 紧急(最高)-URGENT
         * 加急（高）-EXPEDITED
         * 普通-GENERAL
         */
        private String priorityLevel;
        /**
         * 是否可送
         */
        private Boolean isDeliver;
    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 提交发货是否选择渠道
         */
        private Boolean isSelectChannel = false;
        /**
         * 销售单号
         */
        private String code;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 平台名称
         */
        private String dictPlatformName;

        /**
         * 订单状态
         */
        private String billStatus;
        /**
         * 卖家订单编号
         */
        private String sellerOrderCode;
        /**
         * 订单状态名称
         */
        private String billStatusName;

        /**
         * 销售汇率 http://172.16.100.11:3002/project/74/interface/api/19717
         */
        private BigDecimal exchangeRate;

        /**
         * 销售组织id
         */
        private String orgId;

        /**
         * 销售组织名称
         */
        private String orgName;

        /**
         * 平台创建时间
         */
        private String platformOrderCreateTime;

        /**
         * 扩展字段
         */
        private String extendData;
        /**
         * 单据子类型
         */
        @Dict(enumClass = OrderSubTypeEnum.class)
        private String transactionSubType;
        /**
         * 物流信息
         */
        private SoB2cLogisticsDTO.ViewDTO logisticsDTO;
        /**
         * 买家信息
         */
        @Valid
        private SoB2cReceiverDTO.ViewDTO receiverDTO;

        private SoB2cExtendDTO.ViewDTO extendDTO;

        /**
         * 财务信息
         */
        private FinancialInfoDTO financialInfoDTO;

        /**
         * 明细信息
         */
        @NotNull(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDetailDTO.ViewDTO> detailList;
        /**
         * 申报信息
         */
        private List<SoB2cDeclareProductDTO.ViewDTO> declareProductList;

        /**
         * 提交发货时间
         */
        private LocalDateTime createDeliveryTime;

        /**
         * 面单打印时间
         */
        private LocalDateTime finishPrintTime;
    }

    /**
     * 财务信息
     */
    @Data
    @NoArgsConstructor
    public static class FinancialParamDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 是否是人民币
         */
        private Boolean isCny;

        /**
         * 销售订单
         */
        private SoB2cEntity soB2cEntity;

        /**
         * 销售订单物流信息
         */
        private SoB2cLogisticsEntity soB2cLogisticsEntity;

        /**
         * 财务信息
         */
        private SoB2cFinanceEntity soB2cFinanceEntity;

        /**
         * 明细信息
         */
        private List<SoB2cDetailEntity> soB2cDetailList;


    }

    /**
     * 财务信息
     */
    @Data
    @NoArgsConstructor
    public static class FinancialInfoDTO {

        /**
         * 币别
         */
        private String currency;

        /**
         * 订单总金额
         */
        private BigDecimal amount;

        /**
         * 运费收入
         */
        private BigDecimal shippingCost;

        /**
         * 商品成本
         */
        private BigDecimal itemCost;

        /**
         * 商品成本利润率
         */
        private String itemCostProfitRate;

        /**
         * 物流成本
         */
        private BigDecimal logisticsCost;

        /**
         * 物流成本利润率
         */
        private String logisticsCostProfitRate;

        /**
         * 平台费
         */
        private BigDecimal platformCost;

        /**
         * 平台费利润率
         */
        private String platformCostProfitRate;

        /**
         * 转账费
         */
        private BigDecimal paypalCost;

        /**
         * 转账费
         */
        private String paypalCostProfitRate;

        /**
         * 包装辅料费
         */
        private BigDecimal accessoriesCost;

        /**
         * 包装辅料费利润率
         */
        private String accessoriesCostProfitRate;

        /**
         * VAT税费
         */
        private BigDecimal vatCost;

        /**
         * VAT税费利润率
         */
        private String vatCostProfitRate;

        /**
         * 总利润
         */
        private BigDecimal profit;

        /**
         * 总利润率
         */
        private String profitRate;

        /**
         * 总利润率表示
         */
        private BigDecimal profitRateFlag;

        /**
         * 平台费率
         */
        private BigDecimal platformRate;


        /**
         * vat 费率
         */
        private BigDecimal vatRate;


        /**
         * 转账费率
         */
        private BigDecimal transferRate;

        /**
         * 平台费类型
         */
        private String platformCostType;

        /**
         * 转账费类型
         */
        private String transferCostType;

        /**
         * VAT税费类型
         */
        private String vatCostType;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 付款状态（后台拆单使用）
         */
        private String payStatus;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源订单编码
         */
        private String sourceCode;
        /**
         * 扩展字段
         */
        private String extendData;
        /**
         * 标签json
         */
        private String labelJson;

        /**
         * 物流信息
         */
        @Valid
        private SoB2cLogisticsDTO.AddDTO logisticsDTO;
        /**
         * 买家信息
         */
//        @NotNull(message = "买家信息不能为空")
//        @Valid
        private SoB2cReceiverDTO.AddDTO receiverDTO;
        /**
         * 全托管扩展信息
         */
        private SoB2cExtendDTO.AddDTO extendDTO;

        /**
         * 明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDetailDTO.AddDTO> detailList;

        /**
         * SoB2cOptionTypeEnum枚举（拆分、合并）
         */
        private SoB2cOptionTypeEnum operateType;

        /**
         * 订单子类型
         * 接口：oms/common/enumDropDown?type=OrderSubType
         */
        private String transactionSubType;
        /**
         * 第三方编号
         */
        private String thirdCode;

        /**
         * 第三方来源系统
         */
        private String thirdSystem;

        /**
         * 手动添加速卖通订单
         */
        public void checkAndSetAfterTaxAmount() {
            // 速卖通手动单
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(this.getDictPlatform())){
                this.setAfterTaxAmount(this.getAmount());
            }
        }
    }


    /**
     * 订单规则结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleResultDTO{

        private String name;

        private Boolean isRuleMatch;

        /**
         * 是否审核通过
         */
        private Boolean isPass;
        private String id;

        private Map<String,Object> map;

        private List<SoB2cDetailEntity> soB2cDetailList;

        /**
         * 是否自动获取跟踪单号
         */
        private Boolean autoGetTrackNo;

        /**
         *是否自动获取跟踪号提交发货（非超范围派送订单）
         */
        private Boolean autoGetTrackNotOfRangeDelivery;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceResult{
        /**
         * 销售订单信息
         */
        private SoB2cEntity soB2cEntity;
        /**
         * 是否通过
         */
        private Boolean isPass;
    }
    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 订单子类型
         * 接口：oms/common/enumDropDown?type=OrderSubType
         */
        private String transactionSubType;

        /**
         * 物流信息
         */
        private SoB2cLogisticsDTO.UpdateDTO logisticsDTO;
        /**
         * 是否添加买家 true 添加 ，false/null 不添加
         */
        private Boolean addBuyer;
        /**
         * 买家信息
         */
//        @NotNull(message = "买家信息不能为空")
//        @Valid
        private SoB2cReceiverDTO.UpdateDTO receiverDTO;
        /**
         * 全托管扩展信息
         */
        private SoB2cExtendDTO.UpdateDTO extendDTO;

        /**
         * 明细信息
         */
        @NotNull(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 平台订单号
         */
        @Size(max = 100, message = "平台订单号最大长度不能超过100位")
        private String platformCode;

        /**
         * 销售平台
         */
        @NotBlank(message = "销售平台不能为空")
        @Size(max = 32, message = "销售平台最大长度不能超过32位")
        private String dictPlatform;

        /**
         * 店铺
         */
        @NotBlank(message = "店铺不能为空")
        @Size(max = 19, message = "店铺最大长度不能超过19位")
        private String shopId;

        /**
         * 订单金额
         */
        @NotNull(message = "订单金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "订单金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;
        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 币别（原币）
         */
        @NotBlank(message = "币别（原币）不能为空")
        @Size(max = 32, message = "币别（原币）最大长度不能超过32位")
        private String currency;

        /**
         * 付款时间
         */
        private LocalDateTime payTime;

        /**
         * 付款方式
         */
        @Size(max = 32, message = "付款方式最大长度不能超过32位")
        private String dictPayMethod;

        /**
         * 买家备注
         */
        @Size(max = 255, message = "买家备注最大长度不能超过255位")
        private String buyerRemark;

        /**
         * 订单备注
         */
        @Size(max = 255, message = "订单备注最大长度不能超过255位")
        private String remark;

        /**
         * 订单分类
         */
        private List<String> categoryIdList;

        /**
         * 卖家订单编号
         */
        private String sellerOrderCode;

        /**
         * 税后订单金额(速卖通)
         */
        private BigDecimal afterTaxAmount = BigDecimal.ZERO;

    }

    @Data
    @NoArgsConstructor
    public static class SoB2cAddCategoryDTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "选择数据不能为空")
        private List<String> ids;

        /**
         * 编辑分类类型 字典 categoryType类型
         */
        @NotNull(message = "类型不能为空")
        private SoB2cCategoryTypeEnum typeEnum;

        /**
         * 分类id集合
         */
        private List<String> categoryIdList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewSoB2cDistributionDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 销售平台
         */
        private String dictPlatform;

        /**
         * 店铺
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 明细skuId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;
        /**
         * 编码
         */
        private String code;
        /**
         * 原币金额
         */
        private BigDecimal sourceAmount;
        /**
         * 原币别
         */
        private String sourceCurrency;
        /**
         * 本位金额
         */
        private BigDecimal amount;
        /**
         * 本位币别
         */
        private String currency;
        /**
         * 重量
         */
        private BigDecimal weight;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 单个仓库名称
         */
        private String warehouseName;
        /**
         * 物流渠道id
         */
        private String logisticsChannelId;
        /**
         * 物流方式名称
         */
        private String logisticsChannelName;
        /**
         * 物流单号
         */
        private String logisticsCode;

    }


    @Data
    @NoArgsConstructor
    public static class SaveSoB2cDistributionDTO {

        /**
         * 主表id集合
         */
        @NotEmpty(message = "选择数据不能为空")
        private List<String> ids;

        /**
         * 是否覆盖已选择物流和仓库
         */
        private Boolean isCover;

        /**
         * 明细数据
         */
        @Valid
        @NotEmpty(message = "明细数据不能为空")
        private List<SaveSoB2cDistributionDetailDTO> detailList;
    }



    @Data
    @NoArgsConstructor
    public static class SaveSoB2cDistributionDetailDTO {

        @NotBlank(message = "销售订单id不能为空")
        private String id;
        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;
    }

        @Data
    @NoArgsConstructor
    public static class GetLogisticsCode {

        /**
         * 主表id集合
         */
        @NotEmpty(message = "选择数据不能为空")
        private List<String> ids;

        /**
         * 是否提交发货
         */
        private Boolean isDelivery;

    }
    @Data
    @NoArgsConstructor
    public static class GetLogisticsLabel {

        /**
         * 主表id集合
         */
        @NotEmpty(message = "选择数据不能为空")
        private List<String> ids;
    }
    /**
     * 运费估算要的参数
     */
    @Data
    @NoArgsConstructor
    public static class ShippingCalculationDTO {

        /**
         * 起始地
         */
        private String fromCountry;

        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 目的地
         */
        private String toCountryName;

        /**
         * 单位重量
         */
        private String weightUnit;

        /**
         * 包装重量
         */
        private BigDecimal weight;

        /**
         * 长
         */
        private BigDecimal length;

        /**
         * 宽
         */
        private BigDecimal width;

        /**
         * 高
         */
        private BigDecimal height;

        /**
         * 体积
         */
        private BigDecimal volume;


    }


    /**
     * 标记发货的dto
     */
    @Data
    @NoArgsConstructor
    public static class SignShipOrderDTO {

        /**
         * id
         */
        private String id;

        /**
         * 销售订单号
         */
        private String code;

        /**
         * 店铺id
         */
        private String shopId;


        /**
         * 店铺id
         */
        private String shopName;


        /**
         * 交易订单号
         */
        private String platformCode;
        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 渠道对应就是so_b2c_logistics.code
         */
        private String logisticsTransportNo;

        private String logisticsTrackNo;


    }

    /**
     * 合并分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class MergePagingParamDTO extends SortDTO {

        /**
         * 销售单号
         */
        private String code;

        /**
         * 买家名称
         */
        private String buyerName;

        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 收货地址(收货地址1+收货地址2+收货地址3)
         */
        private String address;

        /**
         * 平台集合
         */
        private List<String> platformList;
        /**
         * 店铺id集合
         */
        private List<String> shopIdList;

        /**
         * 国家id集合
         */
        private List<String> countryIdList;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
        
        private String dynamicDataSource;

    }

    /**
     * 合并列表
     */
    @Data
    @NoArgsConstructor
    public static class MergeListDTO {
        /**
         * 平台编码
         */
        private String dictPlatform;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 买家名称
         */
        private String buyerName;
        /**
         * 收货人名称
         */
        private String receiverName;
        /**
         * 国家名称
         */
        private String countyName;
        /**
         * 包装重量
         */
        private BigDecimal weight;
        /**
         * 原币金额
         */
        private BigDecimal sourceAmount;
        /**
         * 原币别
         */
        private String sourceCurrency;
        /**
         * 本位金额
         */
        private BigDecimal amount;
        /**
         * 本位币别
         */
        private String currency;
        /**
         * 物流方式
         */
        private String logisticsChannelId;

        /**
         * 物流方式
         */
        private String logisticsChannelName;

        /**
         * 地址1
         */
        private String firstAddress;
        /**
         * 地址2
         */
        private String secondAddress;
        /**
         * 详细地址
         */
        private String fullAddress;

        /**
         * 出货仓库
         */
        private String warehouseId;

        /**
         * 主表信息
         */
        private List<MergeMainDTO> mainList;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Data
    @NoArgsConstructor
    public static class MergeParamDTO extends SortDTO {

        /**
         * 平台集合
         */
        private List<String> platformList;
        /**
         * 店铺id集合
         */
        private List<String> shopIdList;

        /**
         * 币别集合
         */
        private List<String> currencyList;

        /**
         * 买家名称
         */
        private List<String> buyerNameList;

        /**
         * 收货人
         */
        private List<String> receiverNameList;

        /**
         * 收货地址1
         */
        private List<String> firstAddressList;
        /**
         * 收货地址1
         */
        private List<String> secondAddressList;
        /**
         * 收货地址1
         */
        private List<String> fullAddressList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 物流方式集合
         */
        private List<String> logisticsChannelIdList;
    }

    /**
     * 合并主表信息
     */
    @Data
    @NoArgsConstructor
    public static class MergeMainDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * 销售单号
         */
        private String code;
        /**
         * 平台订单号
         */
        private String platformCode;
        /**
         * 收货人名称
         */
        private String receiverName;
        /**
         * 地址1
         */
        private String firstAddress;
        /**
         * 地址2
         */
        private String secondAddress;
        /**
         * 详细地址
         */
        private String fullAddress;
        /**
         * 明细主键id
         */
        private String detailId;
        /**
         * 图片Url
         */
        private String imageUrl;
        /**
         * 产品skuId
         */
        private String skuId;
        /**
         * 产品sku编号
         */
        private String skuNo;
        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * 平台产品id
         */
        private String platformSpuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 包装重量
         */
        private BigDecimal weight;
        /**
         * 含税成本
         */
        private BigDecimal taxCost;
        /**
         * 订单原币金额
         */
        private BigDecimal sourceAmount;
        /**
         * 原币别
         */
        private String sourceCurrency;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;
        /**
         * 订单本位币金额
         */
        private BigDecimal amount;
        /**
         * 本位币别（默认人民币）
         */
        private String currency;
        /**
         * 出货仓库
         */
        private String warehouseId;
        /**
         * 出货仓库
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 平台编码
         */
        private String dictPlatform;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 买家名称
         */
        private String buyerName;

        /**
         * 物流渠道
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 是否是组数据（第一条标记，前端有用）
         */
        private Boolean isMain;
    }

    /**
     * 合并明细信息
     */
    @Data
    @NoArgsConstructor
    public static class MergeDetailDTO {


    }


    /**
     * 拆分显示
     */
    @Data
    @NoArgsConstructor
    public static class ViewSplitDTO {
        /**
         * 主表id
         */
        private String id;

        /**
         * 销售订单号
         */
        private String code;
        /**
         * 明细信息
         */
        private List<ViewSplitDetailDTO> detailList;
    }

    /**
     * 拆分明细显示
     */
    @Data
    @NoArgsConstructor
    public static class ViewSplitDetailDTO {
        /**
         * 明细id
         */
        private String id;

        /**
         * skuId
         */
        private String skuId;
        /**
         * SKU编码
         */
        private String skuNo;
        /**
         * 平台产品sku
         */
        private String platformSkuNo;
        /**
         * 图片Url
         */
        private String imageUrl;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 原币金额
         */
        private BigDecimal sourceAmount;
        /**
         * 原币别
         */
        private String sourceCurrency;
        /**
         * 本位金额
         */
        private BigDecimal amount;
        /**
         * 本位币别
         */
        private String currency;
        /**
         * 包装重量
         */
        private BigDecimal weight;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 成本来源
         */
        private String costSource;
        /**
         * 材料成本（本位币）
         */
        private BigDecimal productCost;
        /**
         * 头程运费（本位币）
         */
        private BigDecimal firstMileShippingCost;
        /**
         * 清关税费（本位币）
         */
        private BigDecimal clearanceCustomsTax;
    }

    /**
     * 取消拆分验证
     */
    @Data
    @NoArgsConstructor
    public static class CheckCancelSplitDTO {
        /**
         * 拆分前订单
         */
        private String parentB2cSoCode;
        /**
         * 取消拆分明细
         */
        List<CheckCancelSplitDetailDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CheckCancelSplitDetailDTO {

        /**
         * 拆分后订单
         */
        private String childB2cSoCode;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 订单状态
         */
        private String billStatus;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;

    }

    /**
     * 拆分保存
     */
    @Data
    @NoArgsConstructor
    public static class SplitSaveDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 是否同步平台
         */
        private Boolean isSyncPlatform = true;
        /**
         * 明细信息
         */
        @NotEmpty(message = "拆分后订单数据不能为空")
        @Valid
        private List<GroupSplitSaveDTO> groupList;
    }

    @Data
    @NoArgsConstructor
    public static class GroupSplitSaveDTO {


        @NotEmpty(message = "拆分后明细数据不能为空")
        @Valid
        private List<SplitDetailSaveDTO> detailList;
    }


    /**
     * 拆分明细保存
     */
    @Data
    @NoArgsConstructor
    public static class SplitDetailSaveDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "明细id不能为空")
        private String id;
        /**
         * 拆分数量
         */
        @NotNull(message = "拆分数量不能为空")
        @Min(value = 1, message = "拆分数量最小值为1")
        @Max(value = 999999999, message = "拆分数量最大值为999999999")
        private Integer qty;
    }

    @Data
    @NoArgsConstructor
    public static class ViewReceiveDataDTO {
        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名称
         */
        private String name;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 买家电话
         */
        private String telNumber;

        /**
         * 收货地址1
         */
        private String firstAddress;

        /**
         * 收货地址2
         */
        private String secondAddress;

        /**
         * 城市名称
         */
        private String cityName;
        /**
         * 邮编
         */
        private String zipCode;
        /**
         * 国家id
         */
        private String countryId;
        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 收货人名称
         */
        private String receiverName;

        /**
         * 收货人电话
         */
        private String receiverTelNumber;
    }


    @Data
    @NoArgsConstructor
    public static class MatchSkuDTO {

        @NotBlank(message = "sku不能为空")
        private String skuId;

        @NotBlank(message = "明细不能为空")
        private String id;
    }

    /**
     * 打印面单/配货单
     */
    @Data
    @NoArgsConstructor
    public static class WaybillDTO {
        /**
         * B2C销售单号
         */
        private String soB2cId;
        /**
         * 运单号
         */
        private String transportNo;
        /**
         * 跟踪号
         */
        private String trackNo;
        /**
         * 物流面单base64格式
         */
        private List<String> logisticsBase64;
        /**
         * 配货单base64格式
         */
        private List<String> distributeBase64;
    }

    /**
     * 拉取的结果
     */
    @Data
    @NoArgsConstructor
    public static class PullOrderResultDTO {

        private SoB2cEntity soB2cEntity;

        /**
         * 店铺仓库id
         */
        private String shopWarehouseId;

        /**
         * 仓库是否是空的
         */
        private Boolean isWarehouseEmpty;

        /**
         * 订单仓库名称
         */
        private String warehouseName;

        /**
         * 是否是系统新增的订单
         */
        private boolean isNewInsertOrder = false;

        /**
         * 是否是状态变更为取消状态
         */
        private boolean isUpdateCancel = false;

        /**
         * SoB2cErrorTypeEnum.ORDER_FETCH
         */
        SoB2cErrorEntity soB2cError;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {

        private String soCode;


        private String billStatus;

        /**
         * 跟踪号
         */
        private String trackNo;

        /**
         * 销售单号ID
         */
        private String soId;

        /**
         * 是否记录日志
         */
        private boolean addOperationLog = false ;

    }

    @Data
    @NoArgsConstructor
    public static class ShopAuthResultDTO {

        /**
         * 授权类型
         */
        private String authType;

        /**
         * 用户id
         */
        private String userId;
    }

    /**
     * 客户信息
     */
    @Data
    @NoArgsConstructor
    public static class  CustomerDTO{

        private String soId;

        private String shopId;



        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 平台
         */
        private String dictPlatform;


        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 运输单号
         */
        private String transportNo;

        /**
         * 跟踪单号
         */
        private String trackNo;


        /**
         * 付款时间
         */
        private LocalDateTime payTime;
        /**
         * 收货地址 对应 fullAddress
         */
        private String receiverAddress;

        /**
         * 收货人
         */
        private String receiverName;


        /**
         * 交货方式
         */
        private String deliveryMode;

        /**
         * 交货方式
         */
        private String deliveryModeName;


        /**
         * 电话
         */
        private String telNumber;

        /**
         * 销售员
         */
        private  String sellerId;

        /**
         * 对应买家全名
         */
        private String customerName;

        /**
         * 销售员
         */
        private  String sellerName;

        private String salesDeptId;
        private String salesOrgId;

        private String salesOrgName;


        /**
         * 国家
         */
        private String country;

        /**
         * 国家名
         */
        private String countryName;

        /**
         * 是否平台订单
         */
        private Boolean hasPlatformWarehouseOrder;

    }

    /**
     * 拦截订单修改订单信息
     */
    @Data
    @NoArgsConstructor
    public static class InterceptUpdateOrderDTO {
        /**
         * 订单id
         */
        private List<String> ids;
        /**
         * 是否打标拦截
         */
        private Boolean isIntercept;
        /**
         * 是否冻结
         */
        private Boolean isFrozen;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 单据状态
         */
        private String billStatus;

        /**
         * 异常原因
         * 取值：SoB2cAbnormalTypeEnum
         */
        private String abnormalType;

        /**
         * 备注
         */
        private String remark;
    }


    /**
     * 中转报关
     */
    @Data
    @NoArgsConstructor
    public static class TransferDeclareDTO{

        /**
         * 销售订单
         */
        @Size(min = 1 ,message = "销售订单不能为空")
        private List<String> ids;

        /**
         * 报关物流商id  来源 http://172.16.100.11:3002/project/128/interface/api/28035  id
         */
        @NotBlank(message = "报关商不能为空")
        private String transferLogisticsSupplierId;


        /**
         * 报关物流商渠道id http://172.16.100.11:3002/project/128/interface/api/28035  children.id
         */
        @NotBlank(message = "报关商渠道不能为空")
        private String transferLogisticsChannelId;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitDeliveryDTO {
        /**
         * 表 ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        /**
         * 海外仓交运渠道
         */
        private String channelId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SoDeliveryDTO {
        /**
         * 销售订单id
         */
        private String soId;
        /**
         * 发货单号
         */
        private String deliveryCode;
    }


    /**
     * 修改订单发货时间
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDeliveryTimeDTO {
        /**
         * id
         */
        private List<String> soB2cIds;

        private List<SoDeliveryDTO> soDeliveryDTOList;
        /**
         * 状态
         */
        private String status;
        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 物流信息
         */
        List<SoB2cLogisticsEntity> soB2cLogisticsList;
    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ExcelExportDTO {
        /**
         * 销售订单id
         */
        private String id;
        /**
         * 订单创建时间
         */
        private LocalDateTime createTime;
        /**
         * 销售订单明细id
         */
        private String detailId;

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
        /**
         * 单据编码
         */
        private String code;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废原因
         */
        private String invalidRemark;
        /**
         * 作废类型
         */
        private String invalidType;
        /**
         * 作废类型名称
         */
        private String invalidTypeName;
        /**
         * 平台订单状态
         */
        private String platformOrderStatus;
        /**
         * 平台订单状态名称
         * 全托管平台订单状态
         */
        private String platformOrderStatusName;
        /**
         * 是否冻结
         */
        private Boolean isFrozen;
        /**
         * 是否取消（false未取消，true已取消）
         */
        private Boolean isCancel;
        /**
         * 是否地址修改 true 是  false 否
         */
        private Boolean isChangeReceiverAddress;
        /**
         * 是否更换sku true 是  false 否
         */
        private Boolean isChangeSku;
        /**
         * 是否标记不出库发货 true 是  false 否
         */
        private Boolean isNotOutbound;

        /**
         * 销售平台
         */
        private String dictPlatform;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 实际运费(优先实际、没有取预估)
         */
        private BigDecimal shippingCost;

        /**
         * 实际运费币别
         */
        private String shippingCostCurrency;


        /**
         * 总利润
         */
        private BigDecimal totalProfit;

        /**
         * 利润币别（列表默认人民币）
         */
        private String profitCurrency;

        /**
         * 利润率
         */
        private BigDecimal profitRate;

        /**
         * 平台订单号
         */
        private String platformCode;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 单据状态
         */
        private String billStatus;

        /**
         * 单据状态名称
         */
        private String billStatusName;

        /**
         * 订单金额
         */
        private BigDecimal amount;

        /**
         * 币别（原币）
         */
        private String currency;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 付款时间
         */
        private LocalDateTime payTime;

        /**
         * 付款方式
         */
        private String dictPayMethod;

        /**
         * 付款方式名称
         */
        private String dictPayMethodName;

        /**
         * 平台产品ID
         */
        private String platformSpuNo;

        /**
         * 平台SKU
         */
        private String platformSkuNo;

        /**
         * 数量
         */
        private Integer qty;


        /**
         * SKU数量
         */
        private Integer skuQty;

        /**
         * 产品skuId
         */
        private String skuId;
        private String parentSkuId;

        /**
         * 产品sku编号
         */
        private String skuNo;
        /**
         * 父级sku用于记录bom拆分时原sku
         */
        private String parentSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 规格属性
         */
        private String variantProperty;
        /**
         * 属性对象
         */
        private List<SoB2cDetailDTO.PropertyDTO> propertyDTOList;

        /**
         * 含税成本
         */
        private BigDecimal taxCost;

        /**
         * 订单原币金额
         */
        private BigDecimal sourceAmount;
        /**
         * 单价
         */
        private BigDecimal sourcePrice;
        /**
         * 税率
         */
        private BigDecimal taxRate;
        /**
         * 成本来源
         */
        private String costSource;

        /**
         * 币别（原币）
         */
        private String sourceCurrency;

        /**
         * 订单本位币金额
         */
        private BigDecimal baseAmount;

        /**
         * 出货仓库id
         */
        private String  warehouseId;

        /**
         * 出货仓库
         */
        private String  warehouseName;

        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;

        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 买家备注
         */
        private String buyerRemark;

        /**
         * 订单备注
         */
        private String remark;

        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 可用数量
         */
        private Integer useableQty;

        /**
         * 虚拟仓可用数量
         */
        private Integer virtualUsableQty;

        /**
         * 是否缺货
         */
        private Boolean isOutStock;

        /**
         * 卖家订单编号
         */
        private String sellerOrderCode;
        /**
         * 订单分类名称
         */
        private String categoryNames;

        //----------------------------------------------------------物流信息-----------------------------------------------------

        /**
         * 物流渠道名
         */
        private String logisticsChannelName;


        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 包装辅料sku编码
         */
        private String accessoriesSkuNo;
        /**
         * 物流运单号
         */
        private String transportNo;
        /**
         * 物流跟踪单
         */
        private String trackNo;
        /**
         * 物流类型
         */
        private String logisticType;

        /**
         * 买家自选物流名称
         */
        private String name;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 预估运费
         */
        private BigDecimal estimatedShippingCost;

        /**
         * 预估运费币别
         */
        private String estimatedShippingCurrency;

        /**
         * 实际运费
         */
        private BigDecimal actualShippingCost;

        /**
         * 实际运费币别
         */
        private String actualShippingCurrency;

        /**
         * 包装重量
         */
        private BigDecimal weight;

        /**
         * 包装辅料skuId http://172.16.100.11:3002/project/47/interface/api/19600
         */
        private String accessoriesSkuId;

        /**
         * 包装辅料数量
         */
        private Integer accessoriesQty;

        /**
         * 包装辅料净重
         */
        private BigDecimal accessoriesNw;

        /**
         * 包装辅料费
         */
        private BigDecimal accessoriesCost;

        /**
         * 包装辅料费币别
         */
        private String accessoriesCostCurrency;

        /**
         * 长
         */
        private BigDecimal length;

        /**
         * 宽
         */
        private BigDecimal width;

        /**
         * 高
         */
        private BigDecimal height;
        /**
         * ioss税号
         */
        private String iossTaxNo;

        //-------------------------------------------- 买家信息 ---------------------------------------------------------------------------------------

        /**
         * 买家全名
         */
        private String buyerName;

        /**
         * 买家登录id
         */
        private String loginId;

        /**
         * 买家id
         */
        private String customerId;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 买家电话
         */
        private String telNumber;

        /**
         * 收货地址1
         */
        private String firstAddress;

        /**
         * 收货地址2
         */
        private String secondAddress;

        /**
         * 城市名称
         */
        private String cityName;

        /**
         * 国家 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
        private String country;

        /**
         *省/州
         */
        private String provinceName;

        /**
         *区
         */
        private String districtName;

        /**
         * 收货人名称
         */
        private String receiverName;

        /**
         * 收货人电话
         */
        private String receiverTelNumber;

        /**
         * 邮编
         */
        private String postCode;

        /**
         * 街道详细地址
         */
        private String fullAddress;

        /**
         * 收件人税号
         */
        private String receiverTaxNo;

        /**
         * 销售出库时间
         */
        private LocalDate soOutStockTime;
        /**
         * 标签
         */
        private String label;
        /**
         * 明细标签
         */
        private String labelJson;
        /**
         * 拦截订单（ERP发货拦截中，拦截成功，拦截失败的订单）
         */
        private Boolean isIntercept;
        /**
         * 1、拆分生成的子订单 split
         * 2、合并生成的新订单 merge
         */
        private String refType;
        /**
         * WFS（沃尔玛订单shipNodeType=WFSFulfilled或3PLFulfilled）
         */
        private String shipNodeType;
        /**
         * 订单标签集合
         */
        private String labelOrderList;
        /**
         * 明细标签集合
         */
        private String labelDetailList;

        /**
         * 提交发货时间
         */
        private LocalDateTime createDeliveryTime;

        /**
         * 面单打印时间
         */
        private LocalDateTime finishPrintTime;

        //销售订单扩展字段
        //属性字段
        private String extendId;
        /**
         * 要求发货时间
         */
        private LocalDateTime requiredDeliveryTime;
        /**
         * 要求收货时间
         */
        private LocalDateTime requiredReceiveTime;
        /**
         * 发货预警时间
         */
        private LocalDateTime deliveryWarningTime;
        /**
         * 预警时间
         * 未发货时
         * 当前时间< 预警时间时 无异常 黑色
         * 当前时间> 预警时间时 且 当前时间< 要求发货时间 有异常
         * 要求发货时间-当前时间  正数 橙色  负数红色
         * 已发货时
         * 要求发货时间>实际发货时间：则显示未超时
         * 要求发货时间<实际发货时间：则显示已超期N小时
         */
        private BigDecimal warningHour;
        /**
         * 发货预警描述【导出使用】
         */
        private String deliveryWarningDesc;
        /**
         * 订单来源类型
         * SoB2cExtendOrderSourceTypeEnum
         */
        private String orderSourceType;
        private String orderSourceTypeName;

        /**
         * 送货数量
         */
        private Integer deliveryQty;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 上架数量
         */
        private Integer instockQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        //get方法
        private String getLengthStr () {
            return this.length.stripTrailingZeros().toPlainString();
        }
        private String getWidthStr () {
            return this.width.stripTrailingZeros().toPlainString();
        }
        private String getHeightStr () {
            return this.height.stripTrailingZeros().toPlainString();
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemarkDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 拆单保存结果返回
     */
    @Data
    @NoArgsConstructor
    public static class SplitSaveResultDTO {
        /**
         * id
         */
        private List<String> soB2cIds;
        /**
         * 编码
         */
        private List<String> soCodeList;
        /**
         * TikTok拆单入参
         */
        private OrderSplitPramDTO tikTokPramDTO;
        /**
         * 原始订单信息
         */
        private SoB2cEntity oldEntity;

        /**
         * 需要走规则的ids
         */
        private List<SoB2cEntity> needRuleIds;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostPriceDTO {
        /**
         * 修复开始时间
         */
        private LocalDate startTime;

        private List<String> ids;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CombinationDTO{
        private List<SoB2cEntity> soB2cEntityList;
        private List<SoB2cDetailEntity> soB2cDetailEntityList;
    }

    /**
     * 查询b2c销售订单相关信息参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SoB2cDataParamDTO {

        /**
         * 销售订单id集合
         */
        private List<String> b2cSoIdList;

        /**
         * 销售订单编码集合
         */
        private List<String> b2cSoCodeList;


        /**
         * 数据类型集合
         */
        @NotEmpty(message = "数据类型集合不能为空")
        private List<String> dataTypeList;
    }

    /**
     * b2c销售订单相关信息
     */
    @Data
    public static class SoB2cDataDTO {
        /**
         * 主表数据
         */
        private List<SoB2cEntity> list;
        /**
         * 物流信息
         */
        private List<SoB2cLogisticsEntity> logisticsList;
        /**
         * 买家信息
         */
        private List<SoB2cReceiverEntity> receiverList;
    }

    /**
     * 赠品DTO
     */
    @Data
    @NoArgsConstructor
    public static class GiftDTO implements Serializable{
        /**
         * 销售订单id
         */
        @NotBlank(message = "销售订单id不能为空")
        private String id;
        /**
         * 销售订单编码
         */
//        @NotBlank(message = "销售订单编码不能为空")
        private String code;
        /**
         * 赠品skuId
         */
        @NotBlank(message = "赠品skuId不能为空")
        private String skuId;
        /**
         * sku URL
         */
        private String imageUrl;

        /**
         * 赠品skuNo
         */
        @NotBlank(message = "赠品skuNo不能为空")
        private String skuNo;
        /**
         * 赠品数量
         */
        @NotNull(message = "赠品数量不能为空")
        @Min(value = 1,message = "赠品数量最小值为1")
        @Max(value = 999999999,message = "赠品数量最大值为999999999")
        private Integer qty;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;
        /**
         * 仓库名称
         */
        @NotBlank(message = "仓库名称不能为空")
        private String warehouseName;
    }


    /**
     * 子件缺货信息
     */
    @Data
    @NoArgsConstructor
    public static class VirtualChildScarceDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 子件虚拟仓缺货数量
         */
        private Integer virtualScarceQty;

        /**
         * 子件虚拟仓可用数量
         */
        private Integer childUsableQty;

        /**
         * 子级SKU按bom转换后可用数量（父级维度）
         */
        private Integer parentUsableQty;

        /**
         * bom用量
         */
        private Integer quantity;

        /**
         * bom版本
         */
        private String bomVersion;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangeDeliverySkuViewDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        private String code;
        /**
         * 明细ID
         */
        @NotBlank(message = "明细ID不能为空")
        private String detailId;

        @NotBlank(message = "skuId不能为空")
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
         * spuNo
         */
        private String spuNo;
        /**
         * spu名称
         */
        private String spuName;

        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * 更换skuId
         */
        private String changeSkuId;
        /**
         * 更换skuNo
         */
        private String changeSkuNo;
    }

    /**
     * 拆分保存
     */
    @Data
    @NoArgsConstructor
    public static class SplitSkuDTO {
        /**
         * skuNo
         */
        @NotBlank(message = "拆分SKU不能为空")
        private String skuNo;
        /**
         * 拆分明细
         */
        @Valid
        @NotEmpty(message = "拆分明细不能为空")
        private List<SplitSkuDetailDTO> detailList;
    }
    /**
     * 拆分保存
     */
    @Data
    @NoArgsConstructor
    public static class SplitSkuDetailDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;

        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
    }

    @Data
    @NoArgsConstructor
    public static class LogisticsDTO {
        //销售订单id
        private String id;
        //销售订单编码
        private String code;
        //订单金额
        private BigDecimal amount;
        //币别
        private String currency;
        // 汇率
        private BigDecimal exchangeRate;
        /**
         * 是否预估运费超限，是：true  否：false
         */
        private Boolean isOverEstimatedShipCost;
        //渠道id
        private String logisticsChannelId;
        //发货仓库id
        private String warehouseId;
        //长
        private BigDecimal length;
        //宽
        private BigDecimal width;
        //高
        private BigDecimal height;
        //重量
        private BigDecimal weight;
        //重量单位
        private String weightUnit;
        //预估运费
        private BigDecimal estimatedShippingCost;
        //预估运费币种
        private String estimatedShippingCurrency;
        //国家二字码
        private String country;
        //国家名称
        private String countryName;
        //城市名称
        private String cityName;
        //邮编
        private String postCode;
        //省
        private String provinceName;
    }

    @Data
    @NoArgsConstructor
    public static class QueryDTO {
        /**
         * 销售平台类型
         */
        List<String> dictPlatformList;
    }
    @Data
    @NoArgsConstructor
    public static class ExtendDataDTO {
        /**
         * 订单数量
         */
        private Integer orderQty;
        /**
         * 送货数量
         */
        private Integer deliveryQty;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 上架数量
         */
        private Integer instockQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
    }

    @Data
    @NoArgsConstructor
    public static class DeliveryDTO{
        private String id;
        private String code;
        private String dictPlatform;
        private String platformOrderStatus;
        private String shopId;
        private String transportNo;
        private String detailId;
        private String extendData;
        private String extendDetailData;
        private String platformSkuNo;
        private String platformSpuNo;
    }
}