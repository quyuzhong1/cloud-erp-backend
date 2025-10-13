package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.validator.AddGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SoInfoDTO
 * @Date 2023-05-10 17:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoInfoDTO implements Serializable {

    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        /**
         * waitApprove 待审核
         * all 全部
         * waitDelivery 待发货
         * delivery 已发货
         * reject 不通过
         */
        private String searchType;

        private Integer count;
    }

    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String id;
        private String soId;

        private String skuId;

        private String skuNo;

        private BigDecimal price;

        private Integer qty;

        private BigDecimal discountAmount;

        private BigDecimal taxAmountBefore;

        private BigDecimal taxRate;


    }


    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * code
         */
        private String code;

        /**
         * 平台订单编号
         */
        private String platformOrderCode;

        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 平台更新时间
         */
        private LocalDateTime platformUpdateTime;

        /**
         * 平台创建时间
         */
        private LocalDateTime platformCreateTime;

        /**
         * 账户抵扣金额
         */
        private BigDecimal accountDeductAmount;
        /**
         * 返利抵扣金额
         */
        private BigDecimal rebateDeductAmount;
        /**
         * 授信抵扣金额
         */
        private BigDecimal creditDeductAmount;

        /**
         * 订单金额
         */
        private BigDecimal orderAmount;

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
         * 审核状态code
         */
        private BillApproveStatusEnum approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;

        /**
         * 详情id
         */
        private String detailId;


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
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;

        /**
         * 虚拟仓库名称
         */
        private String virtualWarehouseName;

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
         * 目的地
         */
        private String toCountry;

        /**
         * 销售组织id
         */
        private String salesOrgId;

        /**
         * 销售组织名
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
         * 销售员
         */
        private String sellerName;


        /**
         * 发货状态
         */
        private String deliveryStatus;
        /**
         * 发货状态名
         */
        private String deliveryStatusName;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售数量
         */
        private Integer qty;


        /**
         * 缺货数量
         */
        private Integer scarceQty;

        /**
         * 锁定数量（冻结数量）
         */
        private Integer frozenQty;

        /**
         * 缺货数量(虚拟仓)
         */
        private Integer virtualScarceQty;

        /**
         * 子件缺货信息
         */
        private List<VirtualChildScarceDTO> childScarceList;

        /**
         * 是否缺货(虚拟仓)
         */
        private Boolean isVirtualScarce;

        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;

        /**
         * 有效发货通知数量
         */
        private Integer effectiveNoticeQty;

        /**
         * 是否缺货
         * 如果可出数量小于销售数量，即显示缺货标识
         */
        private Boolean isScarce;

        /**
         * 可出数量
         */
        private Integer availableQty;

        /**
         * 已经出库数量
         */
        private Integer deliveryQty;

        /**
         * 剩余数量
         */
        private Integer waitQty;
        /**
         * 申请数量
         */
        private Integer applyQty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 要货 日期
         */
        private LocalDate requireDate;


        /**
         * 销售单价
         */
        private BigDecimal price;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税额
         */
        private BigDecimal tax;

        /**
         * 销售金额
         */
        private BigDecimal amount;


        /**
         * 含税销售金额
         */
        private BigDecimal taxAmount;

        /**
         * 采购单价
         */
        private BigDecimal purchasePrice;

        /**
         * 销售总成本
         */
        private BigDecimal saleCost;

        /**
         * 销售毛利
         */
        private BigDecimal saleProfit;

        /**
         * 销售毛利率
         */
        private BigDecimal saleProfitRate;

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
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;

        /**
         * 备注
         */
        private String remark;
        /**
         * 成本来源
         */
        private String costSource;

        /**
         * 明细备注
         */
        private String detailRemark;
        /**
         * 拣货备注
         */
        private String pickRemark;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 销售金额（本位币）
         */
        private BigDecimal amountLocalCurrency;

        /**
         * 价税合计(本位币)
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 收款账号
         */
        private String receiveAccount;

        /**
         * 收款方式
         */
        private String receiveMethod;

        /**
         * 收款方式
         */
        private String receiveMethodName;

        /**
         * 收款日期
         */
        private LocalDate receiveDate;

        /**
         * 收款金额
         */
        private BigDecimal receiveAmount;

        /**
         * 剩余收款金额
         */
        private BigDecimal remainReceiveAmount;
        /**
         * 贸易条款
         */
        private String tradeTerm;

        /**
         * 贸易条款名称
         */
        private String tradeTermName;

        /**
         * 报关费
         */
        private BigDecimal customsFee;

        /**
         * 银行手续费
         */
        private BigDecimal bankServiceFee;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 仓库组织名称
         */
        private String warehouseOrgName;

        /**
         * 仓库组织id
         */
        private String warehouseOrgId;

        /**
         * 收款账号名称
         */
        private String receiveAccountName;

        /**
         * 折扣总额
         */
        private BigDecimal discountAmount;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 明细折扣金额
         */
        private BigDecimal detailDiscountAmount;

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
         * 收货地址
         */
        private String receiveAddressId;

        /**
         * 交货方式
         */
        private String deliveryMode;

        /**
         * 交货方式
         */
        private String deliveryModeName;

        /**
         * 地址类型
         */
        private String addressType;


        /**
         * 地址类型
         */
        private String addressTypeName;

        /**
         * 收款条件
         */
        private String receiveCondition;

        /**
         * 收款条件
         */
        private String receiveConditionName;

        /**
         * 价税合计（折前）
         */
        private BigDecimal taxAmountBefore;

        /**
         * 是否赠品 true 是
         */
        private Boolean isGift;

        /**
         * 是否补发 true 是
         */
        private Boolean isReissue;

        /**
         * 是否关闭 true 是
         */
        private Boolean isClose;

        /**
         * 客户SKU
         */
        private String platformSkuNo;

        /**
         * 客户订单号
         */
        private String customerOrderNo;

        /**
         * 是否报关
         */
        private Boolean isDeclare;
        /**
         * 物流单上传
         */
        private Boolean isUploadLabel;

        /**
         * 运单号
         */
        private List<String> trackNoList;

        /**
         * 运单号 导出用到
         */
        private String trackNoStr;

        /**
         * 总价税合计（本位币）
         */
        private BigDecimal allAmountLc;

        /**
         * 销售单价(本位币)
         */
        private BigDecimal priceLc;

        /**
         * 含税单价(本位币)
         */
        private BigDecimal taxPriceLc;

        /**
         * 是否组合产品
         */
        private Boolean isConstitute;

        /**
         * 客户PO号
         */
        private String customerPO;
    }

    /**
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
        private Map<String, String> sqlMap;

        /**
         * 高级查询字段
         */
        private List<String> fieldList;

        /**
         * all 全部
         * waitApprove 待审核
         * waitDelivery 待发货
         * delivery 已发货
         * reject 审核不通过
         */
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
         * 是否报关
         */
        private Boolean isDeclare;

        /**
         * 类型
         */
        private String orderType;

        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;

        /**
         * 作废状态
         * true 已作废
         * false 未作废
         */
        private Boolean invalidStatus;

        /**
         * 发货状态状态
         * unShipped 未发货
         * partialShipment 部分发货
         * completeShipment 已发货
         * 来源
         * http://172.16.100.11:3002/project/92/interface/api/9259
         * type=DeliveryStatus
         */
        private String deliveryStatus;

        /**
         * 要货日期集合
         */
        private List<LocalDate> requireDateList;

        /**
         * 客户 集合
         */
        private List<String> customerIdList;

        /**
         * 国家 集合
         */
        private List<String> countryIdList;

        /**
         * 销售员 集合
         */
        private List<String> sellerIdList;

        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 审核时间
         */
        private List<LocalDate> approveTimeList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 备注
         */
        private String remark;

        /**
         * 销售组织
         */
        private String salesOrgId;

        /**
         * 销售部门
         */
        private String salesDeptId;

        /**
         * 平台类型
         */
        private String platformType;

        /**
         * 客户订单号
         */
        private String customerOrderNo;


        /**
         * 运单号
         */
        private String trackNo;


    }


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * id
         * 当暂存的时候会存在
         */
        private String id;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 订单金额
         */
        private BigDecimal orderAmount;

        /**
         * 类型 来源
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=BillType
         */
        @NotBlank(message = "单据类型不能为空", groups = {AddGroup.class})
        @StateEnumValue(clazz = BillTypeEnum.class, message = "单据类型有误", groups = {AddGroup.class})
        private String orderType;

        /**
         * 要货日期
         */
        @NotNull(message = "要货日期不能为空", groups = {AddGroup.class})
        private LocalDate requireDate;

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空", groups = {AddGroup.class})
        private LocalDate billDate;

        /**
         * 组织id
         */
        @NotBlank(message = "销售组织不能为空", groups = {AddGroup.class})
        private String salesOrgId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售员id 来源 http://172.16.100.11:3002/project/36/interface/api/31031
         */
        @NotBlank(message = "销售员不能为空", groups = {AddGroup.class})
        private String sellerId;

        /**
         * 是否收取手续费
         * true 收
         */
        private Boolean isCollectShippingFee;


        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空", groups = {AddGroup.class})
        private String warehouseId;


        /**
         * 银行手续费
         */
        private BigDecimal bankServiceFee;

        /**
         * 运费
         */
        @PositiveOrZero(message = "运费不能为负数", groups = {AddGroup.class})
        private BigDecimal shippingFee;

        /**
         * 是否报关
         */
        @NotNull(message = "是否报关不能为空")
        private Boolean isDeclare;

        /**
         * 报关费
         */
        @PositiveOrZero(message = "报关费不能为负数", groups = {AddGroup.class})
        private BigDecimal customsFee;


        /**
         * 客户id
         */
        @NotBlank(message = "客户不能为空", groups = {AddGroup.class})
        private String customerId;


        /**
         * 收货人
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
         */
        @Size(max = 50, message = "收货人最大50字符")
        private String receiverName;

        /**
         * 电话
         */
        @Size(max = 50, message = "联系电话最大50字符")
        private String telNumber;


        /**
         * 收货人id地址
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
         * <p>
         * 这个是地址下拉 http://172.16.100.11:3002/project/110/interface/api/13786
         */
        private String receiveAddressId;

        /**
         * 交货方式 oms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        @StateEnumValue(strValues = {"deliverGoods", "selfExtraction"}, message = "交货方式有误", groups = {AddGroup.class})
        private String deliveryMode;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空", groups = {AddGroup.class})
        private String currency;

        /**
         * 是否含税
         * true 是
         */
        @NotNull(message = "是否含税不能为空", groups = {AddGroup.class})
        private Boolean isTax;

        /**
         * 地址类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=CustomerAddressType
         */
        @StateEnumValue(strValues = {"forwarder", "receive", "company"}, message = "地址类型有误", groups = {AddGroup.class})
        private String addressType;

        /**
         * 收款账号 接口地址：/oms/bankAccount/select
         */
        private String receiveAccount;

        /**
         * 收款方式  http://172.16.100.11:3002/project/110/interface/api/13435?key=receiveMethod
         */
        private String receiveMethod;

        /**
         * 收款日期
         */
        private LocalDate receiveDate;

        /**
         * 收款条件 http://172.16.100.11:3002/project/110/interface/api/cat_2732
         */
        @NotBlank(message = "收款条件不能为空", groups = {AddGroup.class})
        private String receiveCondition;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255", groups = {AddGroup.class})
        private String remark;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 贸易条款
         */
        private String tradeTerm;

        @Valid
        @Size(min = 1, message = "销售订单详情不能为空", groups = {AddGroup.class})
        private List<SoDetailDTO.AddDTO> detailList;

        /**
         * 折扣总额
         */
        @DecimalMin(value = "0.00", message = "折扣总额不能小于0")
        private BigDecimal discountAmount;

        /**
         * 客户订单号
         */
        private String customerOrderNo;

        /**
         * 平台订单编号
         */
        private String platformOrderCode;
        /**
         * 平台订单Id
         */
        private String platformOrderId;
        /**
         * 单据子类型
         */
        private String transactionSubType;

        /**
         * 来源单据ID
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 平台更新时间
         */
        private LocalDateTime platformUpdateTime;

        /**
         * 平台创建时间
         */
        private LocalDateTime platformCreateTime;

        /**
         * 账户抵扣金额
         */
        private BigDecimal accountDeductAmount;
        /**
         * 返利抵扣金额
         */
        private BigDecimal rebateDeductAmount;
        /**
         * 授信抵扣金额
         */
        private BigDecimal creditDeductAmount;

        /**
         * 收款单信息
         */
        private List<SoReceiptDTO.SoViewDTO> soReceiptDTOList;
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
         * code
         */
        private String code;

        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 平台订单编号
         */
        private String platformOrderCode;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;

        /**
         * 订单金额
         */
        private BigDecimal orderAmount;

        /**
         * 审核状态code
         */
        private BillApproveStatusEnum approveStatus;

        /**
         * 审核状态名
         */
        private String approveStatusName;

        /**
         * 类型
         */
        private String orderType;

        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 组织id
         */
        private String salesOrgId;

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
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库组织id
         */
        private String warehouseOrgId;

        /**
         * 仓库组织名
         */
        private String warehouseOrgName;

        /**
         * 仓库id
         */
        private String warehouseName;


        /**
         * 银行手续费
         */
        private BigDecimal bankServiceFee;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 是否收取运费
         */
        private Boolean isCollectShippingFee;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户id
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
         * 收货人
         */
        private String receiverName;

        /**
         * 电话
         */
        private String telNumber;


        /**
         * 收货人地址
         */
        private String receiveAddress;

        /**
         * 收货人id地址
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
         * <p>
         * 这个是地址下拉 http://172.16.100.11:3002/project/110/interface/api/13786
         */
        private String receiveAddressId;

        /**
         * 交货方式
         */
        private String deliveryMode;


        /**
         * 币种
         */
        private String currency;


        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;

        /**
         * 地址类型
         */
        private String addressType;

        /**
         * 报关费
         */
        private BigDecimal customsFee;

        /**
         * 是否报关
         */
        private Boolean isDeclare;


        /**
         * 收款账号
         */
        private String receiveAccount;

        /**
         * 收款账号描述
         */
        private String receiveAccountName;

        /**
         * 收款方式
         */
        private String receiveMethod;

        /**
         * 收款方式描述
         */
        private String receiveMethodName;

        /**
         * 收款日期
         */
        private LocalDate receiveDate;

        /**
         * 收款金额
         */
        private BigDecimal receiveAmount;

        /**
         * 收款条件
         */
        private String receiveCondition;

        /**
         * 收款条件描述
         */
        private String receiveConditionName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 贸易条款
         */
        private String tradeTerm;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改时间
         */
        private LocalDateTime updateTime;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 销售员名称
         */
        private String sellerName;

        /**
         * 折扣总额
         */
        private BigDecimal discountAmount;

        /**
         * 总价税合计（本位币）
         */
        private BigDecimal allAmountLc;


        /**
         * 客户订单号
         */
        private String customerOrderNo;

        /**
         * 单据子类型
         */
        @Dict(enumClass = OrderSubTypeEnum.class)
        private String transactionSubType;

        /**
         * 版本
         */
        private Integer version;

        /**
         * 收款单信息
         */
        private List<SoReceiptDTO.SoViewDTO> soReceiptDTOList;

        /**
         * 平台订单Id
         */
        private String platformOrderId;

        /**
         * 平台更新时间
         */
        private LocalDateTime platformUpdateTime;

        /**
         * 平台创建时间
         */
        private LocalDateTime platformCreateTime;

        /**
         * 账户抵扣金额
         */
        private BigDecimal accountDeductAmount;
        /**
         * 返利抵扣金额
         */
        private BigDecimal rebateDeductAmount;
        /**
         * 授信抵扣金额
         */
        private BigDecimal creditDeductAmount;

        /**
         * 订单产品详情
         */
        private List<SoDetailDTO.ViewDTO> detailList;
    }


    /**
     * 导出的spi 信息
     */
    @Data
    @NoArgsConstructor
    public static class SoPIDTO {


        /**
         * code
         */
        private String code;


        /**
         * 单据日期
         */
        private LocalDate billDate;


        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 销售员电话
         */
        private String sellerMobile;

        /**
         * 销售员邮箱
         */
        private String sellerEmail;

        /**
         * 客户订单好
         */
        private String customerOrderNo;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 客户地址
         */
        private String address;


        /**
         * 客户邮箱
         */
        private String email;


        /**
         * 客户电话
         */
        private String telNumber;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 收款条件
         */
        private String receiveCondition;

        /**
         * 收款条件
         */
        private String receiveConditionStr;

        /**
         * 销售组织
         */
        private String salesOrgName;

        /**
         * 订单备注
         */
        private String remark;

        /**
         * 运费
         */
        private BigDecimal shippingFee;

        /**
         * 运费
         */
        private String shippingFeeStr;


        /**
         * 总数量
         */
        private Integer totalQty;

        /**
         * 总含税金额
         */
        private Integer totalTaxAmount;

        /**
         * 总不含税金额
         */
        private Integer totalAmount;

        /**
         * 总金额
         */
        private String totalAmountStr;

        /**
         * 总费用
         */
        private BigDecimal totalFee;

        /**
         * 总费用
         */
        private String totalFeeStr;


        /**
         * 贸易条款
         */
        private String tradeTerm;


        /**
         * 通讯地址
         */
        private String mailAddress;

    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotBlank(message = "id不能为空")
        private String id;


        /**
         * 平台
         */
        private String dictPlatform;

        private String platformOrderId;
        /**
         * 类型 来源
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=BillType
         */
        @NotBlank(message = "单据类型不能为空", groups = {AddGroup.class})
        @StateEnumValue(clazz = BillTypeEnum.class, message = "单据类型有误", groups = {AddGroup.class})
        private String orderType;

        /**
         * 订单金额
         */
        private BigDecimal orderAmount;
        /**
         * 要货日期
         */
        @NotNull(message = "要货日期不能为空", groups = {AddGroup.class})
        private LocalDate requireDate;

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空", groups = {AddGroup.class})
        private LocalDate billDate;

        /**
         * 组织id
         */
        @NotBlank(message = "销售组织不能为空", groups = {AddGroup.class})
        private String salesOrgId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 平台订单编号
         */
        private String platformOrderCode;

        /**
         * 销售员id
         */
        @NotBlank(message = "销售员不能为空", groups = {AddGroup.class})
        private String sellerId;

        /**
         * 是否收取手续费
         * true 收
         */
        private Boolean isCollectShippingFee;


        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空", groups = {AddGroup.class})
        private String warehouseId;


        /**
         * 银行手续费
         */
        private BigDecimal bankServiceFee;

        /**
         * 运费
         */
        @PositiveOrZero(message = "运费不能为负数", groups = {AddGroup.class})
        private BigDecimal shippingFee;

        /**
         * 报关费
         */
        @PositiveOrZero(message = "报关费不能为负数", groups = {AddGroup.class})
        private BigDecimal customsFee;

        /**
         * 是否报关
         */
        @NotNull(message = "是否报关不能为空")
        private Boolean isDeclare;

        /**
         * 客户id
         */
        @NotBlank(message = "客户不能为空", groups = {AddGroup.class})
        private String customerId;


        /**
         * 收货人
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
         */
        @Size(max = 50, message = "收货人最大50字符")
        private String receiverName;

        /**
         * 电话
         */
        @Size(max = 50, message = "联系电话最大50字符")
        private String telNumber;

        /**
         * 平台更新时间
         */
        private LocalDateTime platformUpdateTime;

        /**
         * 平台创建时间
         */
        private LocalDateTime platformCreateTime;

        /**
         * 账户抵扣金额
         */
        private BigDecimal accountDeductAmount;
        /**
         * 返利抵扣金额
         */
        private BigDecimal rebateDeductAmount;
        /**
         * 授信抵扣金额
         */
        private BigDecimal creditDeductAmount;

        /**
         * 收货人id地址
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
         * <p>
         * 这个是地址下拉 http://172.16.100.11:3002/project/110/interface/api/13786
         */
        private String receiveAddressId;

        /**
         * 交货方式 oms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        @StateEnumValue(strValues = {"deliverGoods", "selfExtraction"}, message = "交货方式有误", groups = {AddGroup.class})
        private String deliveryMode;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空", groups = {AddGroup.class})
        private String currency;

        /**
         * 是否含税
         * true 是
         */
        @NotNull(message = "是否含税不能为空")
        private Boolean isTax;

        /**
         * 地址类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=CustomerAddressType
         */
        @StateEnumValue(strValues = {"forwarder", "receive", "company"}, message = "地址类型有误", groups = {AddGroup.class})
        private String addressType;

        /**
         * 收款账号 接口地址：/oms/bankAccount/select
         */
        private String receiveAccount;

        /**
         * 收款方式  http://172.16.100.11:3002/project/110/interface/api/13435?key=receiveMethod
         */
        private String receiveMethod;

        /**
         * 收款日期
         */
        private LocalDate receiveDate;

        /**
         * 收款条件 http://172.16.100.11:3002/project/110/interface/api/13435?key=collectionTerms
         */
        private String receiveCondition;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255", groups = {AddGroup.class})
        private String remark;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 贸易条款
         */
        private String tradeTerm;

        @Valid
        @Size(min = 1, message = "销售订单详情不能为空", groups = {AddGroup.class})
        private List<SoDetailDTO.UpdateDTO> detailList;

        /**
         * 折扣总额
         */
        @DecimalMin(value = "0.00", message = "折扣总额不能小于0")
        private BigDecimal discountAmount;

        /**
         * 客户订单号
         */
        private String customerOrderNo;

        /**
         * 单据子类型
         */
        @Dict(enumClass = OrderSubTypeEnum.class)
        private String transactionSubType;

        /**
         * 收款单信息
         */
        private List<SoReceiptDTO.SoViewDTO> soReceiptDTOList = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;

        /**
         * 无权限字段
         */
        private List<String> nopermitFields;
    }

    /**
     * 销售订单的客户信息
     */
    @Data
    @NoArgsConstructor
    public static class CustomerDTO {


        private String id;
        /**
         * 单据类型
         */
        private String orderType;

        /**
         * 折扣总额
         */
        private BigDecimal discountAmount;

        private String code;

        private Boolean invalidStatus;

        private LocalDateTime createTime;

        private LocalDate billDate;


        private String countryId;

        /**
         * 审核状态
         */
        private BillApproveStatusEnum approveStatus;

        /**
         * 单据类型名称
         */
        private String orderTypeName;
        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户id
         */
        private String customerName;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 收货人地址
         */
        private String receiveAddress;

        /**
         * 收货人地址
         */
        private String receiveAddressId;

        /**
         * 交货方式
         */
        private String deliveryMode;

        /**
         * 交货方式
         */
        private String deliveryModeName;


        /**
         * 币种
         */
        private String currency;


        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;

        /**
         * 地址类型
         */
        private String addressType;

        /**
         * 地址类型名
         */
        private String addressTypeName;


        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 销售组织
         */
        private String salesOrgName;

        private String salesOrgId;


        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门
         */
        private String salesDeptName;


        /**
         * 销售员
         *
         * @author yl
         * @date 2023-05-18 10:24
         * @param null
         * @return
         */
        private String sellerName;

        private String sellerId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库存组织id
         */
        private String warehouseOrgId;

        /**
         * 库存组织名称
         */
        private String warehouseOrgName;

        /**
         * 销售备注
         */
        private String soRemark;
        /**
         * 客户备注
         */
        private String customerRemark;
        /**
         * 销售平台
         */
        private String dictPlatform;

        private String syncKingdeeId;

        /**
         * 客户关联的销售员
         */
        private String customerSellerId;

        /**
         * 纳税登记号
         */
        private String taxRegisterCode;

        /**
         * 通讯地址
         */
        private String mailAddress;
    }


    /**
     * 导出销售订单的信息
     */
    @Data
    @NoArgsConstructor
    public static class ExportPdfDTO {

        /**
         * 合同号
         */
        private String code;

        /**
         * 甲方(购方)
         */
        private String customerName;

        /**
         * 纳税人识别号 (甲方)
         */
        private String taxpayerId;

        /**
         * 联系人 (甲方)
         */
        private String contactPerson;

        /**
         * 联系 电话 (甲方)
         */
        private String contactTelNumber;

        /**
         * 联系地址 (甲方)
         */
        private String contactAddress;

        /**
         * 合计
         */
        private BigDecimal totalAmount;

        /**
         * 价税合计
         */
        private BigDecimal totalTaxAmount;
        /**
         * 大写
         */
        private String chineseAmount;

        /**
         * 合计数据
         */
        private Integer totalQty;

        /**
         * 币别
         */
        private String currency;


        /**
         * 签订日期(甲方)
         */
        private LocalDate firstSignDate;

        /**
         * 签订日期（乙方）
         */
        private LocalDate secondSignDate;


        //乙方
        private String company;

        //乙方 纳税识别号
        private String companyTaxpayerId;

        //乙方 联系人
        private String sellerName;

        //乙方 联系电话
        private String sellerTelNumber;

        //乙方 地址
        private String companyAddress;

        //明细
        private List<SoDetailDTO.ExportPdfDTO> details;


    }

    @Data
    @NoArgsConstructor
    public static class ViewGenerateSalesDemandDTO {

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 仓库id
         */
        private String warehouseId;

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
         * 销售数量
         */
        private Integer qty;

        /**
         * 缺货数量
         */
        private Integer scarceQty;

        /**
         * 备货数量
         */
        private Integer planStockQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 是否是一级数据
         */
        private Boolean flag;
    }

    /**
     * 下推发货通知单\销售出库单 列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateDeliveryView {
        /**
         * 明细id
         */
        @NotBlank(message = "明细不能为空")
        private String detailId;

        /**
         * 主表id
         * 销售订单id
         */
        @NotBlank(message = "销售订单不能为空")
        private String soId;

        /**
         * 销售单号
         */
        @NotBlank(message = "销售订单编号不能为空")
        private String soCode;

        /**
         * 库存组织id
         */
        private String inventoryOrgId;

        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 客户sku
         */
        private String platformSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售数量
         */
        private Integer salesQty;

        /**
         * 已下推发货数量
         */
        private Integer alreadyDeliveryQty;

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
         * 是否关闭
         */
        private Boolean isClose;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 下推销售出库订单-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoOutView {

        /**
         * 主表id
         */
        private String id;

        /**
         * 销售单号
         */
        private String code;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售员名称
         */
        private String sellerName;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;

        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库组织id
         */
        private String warehouseOrgId;
        /**
         * 仓库组织名称
         */
        private String warehouseOrgName;
        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        private String warehouseLocation;

        /**
         * 单机日期
         */
        private LocalDate billDate;

        /**
         * 销售数量
         */
        private Integer salesQty;

        /**
         * 待发数量
         */
        private Integer waitDeliveryQty;

        /**
         * 实发数量
         */
        @NotNull(message = "实发数量不能为空")
        private Integer actualDeliveryQty;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 下推退货订单-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoReturnView {
        /**
         * 明细id
         */
        private String detailId;

        /**
         * 主表id
         */
        private String soId;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;

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
         * 销售数量
         */
        private Integer salesQty;

        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        private Integer returnQty;

        /**
         * 已出库数量
         */
        private Integer deliveryQty;

        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         * ReturnTypeEnum
         */
        @NotBlank(message = "退货类型不能为空")
        private String returnTypeDict;

        /**
         * 退货原因 wms/common/enumDropDown?type=ReturnReason
         */
        private String returnReasonDict;

        /**
         * 退货日期
         */
        @NotNull(message = "退货日期不能为空")
        private LocalDate returnDate;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 备注
         */
        private String remark;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;
        /**
         *退货金额
         */
        @NotNull(message = "退货金额不能为空")
        private BigDecimal returnAmount;
        /**
         *含税退货金额
         */
        @NotNull(message = "含税退货金额不能为空")
        private BigDecimal taxReturnAmount;
        /**
         *销售金额
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
         * 销售组织id
         */
        private String salesOrgId;
        /**
         * 库存组织id
         */
        private String warehouseOrgId;
        /**
         * 退货物流单号
         */
        @NotBlank(message = "退货物流单号不能为空")
        private String returnLogisticCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingTotalDTO {

        /**
         * 合计数量
         */
        private Integer totalQty;

        /**
         * 合计金额
         */
        private BigDecimal totalAmount;

        /**
         * 合计含税金额
         */
        private BigDecimal totalTaxAmount;

        /**
         * 销售金额(本位币)
         */
        private BigDecimal amountLocalCurrency;

        /**
         * 价税合计(本位币)
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         * 发货数量
         */
        private Integer totalDeliveryQty;

        /**
         * 待发货数量
         */
        private Integer totalWaitQty;

        /**
         * 锁定数量
         */
        private Integer totalFrozenQty;
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

    @Data
    @NoArgsConstructor
    public static class DetailCalDTO {

        /**
         * 序号
         */
        private Integer idx;

        /**
         * 价税合计（折前）
         */
        private BigDecimal taxAmount;

        /**
         * 折扣额
         */
        private BigDecimal detailDiscountAmount;

        /**
         * 价税合计折扣比例
         */
        private BigDecimal taxAmountRate;

    }

    /**
     * 计算毛利成本数据
     */
    @Data
    @NoArgsConstructor
    public static class CalCostProfitDTO {
        @NotNull(message = "销售组织不能为空")
        private String salesOrgId;
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;

        /**
         * 折扣总额
         */
        @DecimalMin(value = "0.00", message = "折扣总额不能小于0")
        private BigDecimal discountAmount;

        @Valid
        @Size(min = 1, message = "销售订单详情不能为空")
        private List<SoDetailDTO.CalDetailDTO> detailList;

    }

    /**
     * 单个锁定
     */
    @Data
    @NoArgsConstructor
    public static class LockVirtualInventoryDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * 客户
         */
        private String customerName;
        /**
         * 销售单号
         */
        private String code;
        /**
         * 单据类型
         */
        private String orderTypeName;
        /**
         * 销售组织
         */
        private String salesOrgName;
        /**
         * 销售员
         */
        private String  sellerName;
        /**
         * 销售员部门
         */
        private String salesDeptName;
        /**
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 订单备注
         */
        private String remark;
        /**
         * 实体仓
         */
        private String warehouseName;
        /**
         * 虚拟仓
         */
        private String virtualWarehouseName;
        /**
         * 明细
         */
        private List<LockVirtualInventoryDetailDTO> detailList;
    }


    /**
     * 单个锁定明细
     */
    @Data
    @NoArgsConstructor
    public static class LockVirtualInventoryDetailDTO {
        /**
         * 明细id
         */
        private String detailId;
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
        private Integer qty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
        /**
         * 最大可锁数(可冻结数量)
         */
        private Integer toFrozenQty;
        /**
         * 锁定数量(冻结数量)
         */
        private Integer frozenQty;
        /**
         * 缺货数量
         */
        private Integer virtualScarceQty;
        /**
         * 发货通知数量（总数量）
         */
        private Integer totalNoticeQty;
        /**
         * 发货通知数量（已审核）
         */
        private Integer effectiveNoticeQty;
        /**
         * 已出库数量
         */
        private Integer outstockQty;
        /**
         * 未出库数量
         */
        private Integer unOutstockQty;
        /**
         * 备注
         */
        private String remark;

        /**
         * 是否组合品
         */
        private Boolean isCombination;
        /**
         * 子件缺货相信
         */
        private List<VirtualChildScarceDTO> childScarceList;
    }

    /**
     * 批量锁定
     */
    @Data
    @NoArgsConstructor
    public static class BatchLockVirtualInventoryDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 客户
         */
        private String customerName;
        /**
         * 销售单号
         */
        private String code;
        /**
         * 销售员
         */
        private String  sellerName;

        /**
         * 要货日期
         */
        private LocalDate requireDate;
        /**
         * 实体仓
         */
        private String warehouseName;
        /**
         * 虚拟仓
         */
        private String virtualWarehouseName;
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
        private Integer qty;
        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;
        /**
         * 剩余最大可锁数(可冻结数量)
         */
        private Integer toFrozenQty;
        /**
         * 锁定数量(冻结数量)
         */
        private Integer frozenQty;
        /**
         * 缺货数量
         */
        private Integer virtualScarceQty;
        /**
         * 发货通知数量（总数量）
         */
        private Integer totalNoticeQty;
        /**
         * 发货通知数量（已审核）
         */
        private Integer effectiveNoticeQty;
        /**
         * 已出库数量
         */
        private Integer outstockQty;
        /**
         * 未出库数量
         */
        private Integer unOutstockQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 明细备注
         */
        private String detailRemark;

        /**
         * 是否组合品
         */
        private Boolean isCombination;
        /**
         * 子件缺货相信
         */
        private List<VirtualChildScarceDTO> childScarceList;
    }

    @Data
    @NoArgsConstructor
    public static class LockVirtualInventorySaveDTO {

        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;

        /**
         * 冻结数量
         */
        @NotNull(message = "请填写锁定数量")
        @Min(value = 1,message = "锁定数量最小值为1")
        @Max(value = 999999999,message = "锁定数量最大值为999999999")
        private Integer frozenQty;
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

    /**
     * BOM缺货返回信息
     */
    @Data
    @NoArgsConstructor
    public static class VirtuaParamScarceDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 锁定数量
         */
        private Integer frozenQty;

        /**
         * 缺货数量(虚拟仓)
         */
        private Integer virtualScarceQty;

        /**
         * 是否组合品
         */
        private Boolean isCombination;


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;

        /**
         * 是否缺货(虚拟仓)
         */
        private Boolean isVirtualScarce;

        /**
         * 虚拟仓可用库存
         */
        private Integer virtualUsableQty;

        /**
         * 子件缺货信息
         */
        private List<VirtualChildScarceDTO> childScarceList;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class CalDetailDTO {
        /**
         * 明细
         */
        private List<CalDTO> details;

    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class CalDTO {
        /**
         * 明细id
         */
        private String detailId;

        /**
         * 退货数量
         */
        private Integer returnQty;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateApprovalStatusDTO {
        private SoInfoEntity soInfoEntity;
        private BillApproveStatusEnum  billApproveStatusEnum;
    }


    @Data
    @NoArgsConstructor
    public static class UpdatePlatformOrderIdDTO {
        /**
         * 销售订单编码
         */
        @NotBlank(message = "销售订单编码")
        private String soCode;

        /**
         * 平台订单Id
         */
        @NotBlank(message = "平台订单Id不能为空")
        private String platformOrderId;

        /**
         * 平台订单明细ID
         */
        @NotEmpty(message = "平台订单明细Id不能为空")
        private List<String> platformDetailIdList;
    }
}
