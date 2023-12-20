package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cFinanceEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cCategoryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
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


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 （all全部，payment待付款，pending待处理，approveIng审核中，inDistribution配货中，waitShipped代发货，shipped已发货，frozen冻结中，invalid已作废）
         */
        private String tabFlag;

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
         * 搜索类型（all全部，payment待付款，pending待处理，approveIng审核中，inDistribution配货中，waitShipped代发货，shipped已发货，frozen冻结中，invalid已作废）
         */
        private String tabFlag;
        /**
         * 销售单号
         */
        private String code;
        /**
         * 平台订单号
         */
        private String platformCode;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 平台集合（platform字典类型）http://172.16.100.11:3002/project/110/interface/api/13435
         */
        private List<String> platformList;
        /**
         * 店铺id集合 http://172.16.100.11:3002/project/110/interface/api/16513
         */
        private List<String> shopIdList;
        /**
         * 国家id集合 http://172.16.100.11:3002/project/36/interface/api/13390
         */
        private List<String> countryList;
        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * 平台产品id
         */
        private String platformSpuNo;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 订单状态 （soB2cBillStatus字典类型）http://172.16.100.11:3002/project/110/interface/api/13435
         */
        private List<String> billStatusList;
        /**
         * 付款状态 （soB2cPayStatus字典类型）http://172.16.100.11:3002/project/110/interface/api/13435
         */
        private List<String> payStatusList;
        /**
         * 分类集合 http://172.16.100.11:3002/project/110/interface/api/19699
         */
        private List<String> categoryList;
        /**
         * 标签集合 （soB2cLable字典类型）http://172.16.100.11:3002/project/110/interface/api/13435
         */
        private List<String> labelList;
        /**
         * 异常信息集合（soB2cAbnormalType字典类型）http://172.16.100.11:3002/project/110/interface/api/13435
         */
        private List<String> abnormalTypeList;
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
         * 单据编码
         */
        private String code;

        /**
         * 销售平台
         */
        private String dictPlatform;

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
         * 国家
         */
        private String countryName;

        /**
         * 是否对接了第三方海外仓
         * true 是
         */
        private Boolean isOverseasProviderWarehouse;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名
         */
        private String logisticsChannelName;

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
         * 物流单号
         */
        private String logisticsCode;

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
         * 来源类型
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
         * b2c销售订单明细信息
         */
        private List<SoB2cDetailDTO.ListDTO> detailList;
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
        private String FulfillmentChannel;
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
         * FBA（亚马逊订单FulfillmentChannel=AFN-亚马逊配送时）
         */
        private String FulfillmentChannel;
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

        /**
         * 是否平台仓订单 true 是 fasle 不是
         */
        private Boolean isAliexpressPlatformWarehouseOrder;
    }

    /**
     * 导出Excel
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
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
         * 销售单号
         */
        private String code;

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
         * 订单状态名称
         */
        private String billStatusName;

        /**
         * 销售汇率 http://172.16.100.11:3002/project/74/interface/api/19717
         */
        private BigDecimal exchangeRate;

        /**
         * 物流信息
         */
        private SoB2cLogisticsDTO.ViewDTO logisticsDTO;
        /**
         * 买家信息
         */
        @Valid
        private SoB2cReceiverDTO.ViewDTO receiverDTO;

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
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 来源订单id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源订单编码
         */
        private String sourceCode;

        /**
         * 物流信息
         */
        private SoB2cLogisticsDTO.AddDTO logisticsDTO;
        /**
         * 买家信息
         */
        @NotNull(message = "买家信息不能为空")
        @Valid
        private SoB2cReceiverDTO.AddDTO receiverDTO;

        /**
         * 明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDetailDTO.AddDTO> detailList;
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
         * 物流信息
         */
        private SoB2cLogisticsDTO.UpdateDTO logisticsDTO;
        /**
         * 买家信息
         */
        @NotNull(message = "买家信息不能为空")
        @Valid
        private SoB2cReceiverDTO.UpdateDTO receiverDTO;

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
        @Size(max = 32, message = "平台订单号最大长度不能超过32位")
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
         * 币别（原币）
         */
        @NotBlank(message = "币别（原币）不能为空")
        @Size(max = 32, message = "币别（原币）最大长度不能超过32位")
        private String currency;

        /**
         * 付款时间
         */
        @NotNull(message = "付款时间不能为空")
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
         * 仓库（逗号分隔）
         */
        private String warehouseNames;
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
         * 物流渠道id
         */
        @NotBlank(message = "物流渠道不能为空")
        private String logisticsChannelId;

        /**
         * 仓库 http://172.16.100.11:3002/project/92/interface/api/22930
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
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
        private String logisticsBase64;
        /**
         * 配货单base64格式
         */
        private String distributeBase64;
    }


}