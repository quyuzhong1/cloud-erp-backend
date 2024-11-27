package com.erp.model.oms.dto;

import com.common.business.validator.AddGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname SoInfoDTO
 * @Date 2023-05-10 17:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoDetailDTO implements Serializable {


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        private String id;
        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空", groups = {AddGroup.class})
        private String skuId;

        /**
         * 销售数量
         */
        @NotNull(message = "销售数量不能为空", groups = {AddGroup.class})
        @DecimalMax(value = "999999999", message = "销售数量最大值", groups = {AddGroup.class})
        @DecimalMin(value = "1", message = "销售数量最小值不能为0", groups = {AddGroup.class})
        private Integer qty;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空", groups = {AddGroup.class})
        private String currency;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 是否赠品
         */
        @NotNull(message = "是否赠品不能为空", groups = {AddGroup.class})
        private Boolean isGift;

        /**
         * 是否补发
         */
        @NotNull(message = "是否补发不能为空", groups = {AddGroup.class})
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;


        /**
         * 平台sku no
         */
        private String platformSkuNo;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大200字符")
        private String remark;

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
         * 销售金额(本位币)
         */
        private BigDecimal amountLocalCurrency;

        /**
         * 价税合计(本位币)
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         * 价税合计（折前）
         */
        private BigDecimal taxAmountBefore;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         * <p>
         * 对与销售出库单 以及下推的单据
         * 这个id 就是
         * sourceDetailId
         */
        private String id;


        /**
         * 第三方仓SKU
         */
        private String thirdWarehouseSku;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 平台sku no
         */
        private String platformSkuNo;

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
         * 冻结数量
         */
        private Integer frozenQty;

        /**
         * 虚拟仓缺货数量
         */
        private Integer virtualScarceQty;

        /**
         * 可出数量
         */
        private Integer availableQty;

        /**
         * 虚拟仓可出数量
         */
        private Integer virtualAvailableQty;

        /**
         * 已经出库数量
         */
        private Integer deliveryQty;

        /**
         * 剩余未出数量
         */
        private Integer waitQty;


        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;


        /**
         * 单价
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
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 单位
         */
        private String unit;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 含税的销售金额
         */
        private BigDecimal taxAmount;

        /**
         * 及时库存
         */
        private Integer curInventoryQty;


        /**
         * 历史最高
         */
        private BigDecimal maxPrice;

        /**
         * 历史最低
         */
        private BigDecimal minPrice;

        /**
         * 平均价格
         */
        private BigDecimal avgPrice;


        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否补发
         */
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;


        /**
         * 备注
         */
        private String remark;

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
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 销售金额(本位币)
         */
        private BigDecimal amountLocalCurrency;

        /**
         * 价税合计(本位币)
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         * 销售金额汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 折扣金额
         */
        private BigDecimal discountAmount;

        /**
         * 价税合计（折前)
         */
        private BigDecimal taxAmountBefore;

        /**
         * 销售单价(本位币)
         */
        private BigDecimal priceLc;

        /**
         * 含税单价(本位币)
         */
        private BigDecimal taxPriceLc;

    }

    /**
     * 发票信息的
     */
    @Data
    @NoArgsConstructor
    public static class ViewPiDTO {


        private Integer no;

        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 规格类型
         */
        private String model;

        private Integer qty;

        private BigDecimal price;

        private BigDecimal taxPrice;

        private String currencySymbol;

        private String priceStr;

        /**
         * 含税单价
         */
        private String taxPriceStr;

        private BigDecimal amount;

        private BigDecimal taxAmount;
        private String amountStr;

        private String skuId;

        private String desc;

        /**
         * 主要材料
         */
        private String materials;

        private String imageUrl;


    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class SkuHistoryPriceDTO {
        /**
         * sku id
         */
        private String skuId;

        /**
         * 次数
         */
        private String count;

        /**
         * 历史最高
         */
        private BigDecimal maxPrice;

        /**
         * 历史最低
         */
        private BigDecimal minPrice;

        /**
         * 平均价格
         */
        private BigDecimal avgPrice;

    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {


        private String id;


        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * 销售数量
         */
        @NotNull(message = "销售数量不能为空")
        @DecimalMax(value = "999999999", message = "销售数量最大值")
        private Integer qty;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 是否赠品
         */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift;

        /**
         * 是否补发
         */
        @NotNull(message = "是否补发不能为空")
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;

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
         * 销售金额(本位币)
         */
        private BigDecimal amountLocalCurrency;

        /**
         * 价税合计(本位币)
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大200字符")
        private String remark;
    }


    /**
     * 临时修改数据 的参数
     */
    @Data
    @NoArgsConstructor
    public static class TempUpdateDTO {

        /**
         * 详情id
         */
        private String id;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 单价日期
         */
        private LocalDate BillDate;

        /**
         * 折扣总额
         */
        private BigDecimal discountAmount;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 币种
         */
        private String currency;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否补发
         */
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;

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
         * 备注
         */
        @Size(max = 200, message = "备注最大200字符")
        private String remark;
    }


    /**
     * SKU 信息
     */
    @Data
    @NoArgsConstructor
    public static class SkuDTO {


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
         * 销售数量
         */
        private Integer qty;

        /**
         * 缺货数量
         */
        private Integer scarceQty;

        /**
         * 虚拟仓缺货数量
         */
        private Integer virtualScarceQty;

        /**
         * 可出数量
         */
        private Integer availableQty;

        /**
         * 虚拟仓可出数量
         */
        private Integer virtualAvailableQty;

        /**
         * 已经出库数量
         */
        private Integer deliveryQty;

        /**
         * 剩余数量
         */
        private Integer waitQty;


        /**
         * 单价
         */
        private BigDecimal price;


        /**
         * 税率
         */
        private BigDecimal taxRate;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 单位
         */
        private String unit;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 价税销售金额
         */
        private BigDecimal taxAmount;

        /**
         * 及时库存
         */
        private Integer curInventoryQty;


        /**
         * 历史最高
         */
        private BigDecimal maxPrice;

        /**
         * 历史最低
         */
        private BigDecimal minPrice;

        /**
         * 平均价格
         */
        private BigDecimal avgPrice;


        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否补发
         */
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;


        /**
         * 备注
         */
        private String remark;


    }

    /**
     * SKU 信息
     */
    @Data
    @NoArgsConstructor
    public static class ListSkuParamDTO {


        /**
         * skuNo List
         */
        @Size(min = 1, message = "sku至少需要一个")
        @NotNull(message = "sku不能为空")
        private List<String> skuNoList;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
    }


    /**
     * 导入返回
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * 成功返回数据
         */
        private List<SkuDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ListAddDetailNoBomViewDTO {

        /**
         *
         */
        private List<String> parentSkuNoList;
        /**
         *
         */
        private List<AddDetailView> nobomList;

        /**
         *
         */
        private List<AddDetailView> bomList;

        /**
         * 存在套装BOM
         */
        private Boolean existBom = false;
    }

    /**
     * 添加详情按钮-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class AddDetailView {
        /**
         * id
         */
        private String id;
        /**
         * 销售单Id
         */
        private String soId;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * skuId
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
         * 变体名称
         */
        private String variantProperty;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库Name
         */
        private String warehouseName;
        /**
         * 库存组织id
         */
        private String inventoryOrgId;
        /**
         * 库存组织名称
         */
        private String inventoryOrgName;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 销售金额
         */
        private BigDecimal salesAmount;
        /**
         * 币种
         */
        private String currency;
        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * 可出数量
         */
        private Integer availableQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 剩余未出数量
         */
        private Integer unDeliveryQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 应退数量
         */
        private Integer mustQty;
        /**
         * 是否赠品 true 是
         */
        private Boolean isGift;
        /**
         * 是否关闭 true 是
         */
        private Boolean isClose;
        /**
         * 备注
         */
        private String remark;
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
        private String returnReasonDict;
        /**
         * 退货原因名称
         */
        private String returnReasonDictName;
        /**
         * 退货客户id
         */
        private String customerId;
        /**
         * 平台sku
         */
        private String platformSkuNo;
        /**
         * 是否子sku
         */
        private Boolean isChildSkuNo = false;
        /**
         *退货金额
         */
        private BigDecimal returnAmount;
        /**
         *含税退货金额
         */
        private BigDecimal taxReturnAmount;
        /**
         *退货金额（本位币）
         */
        private BigDecimal returnAmountLocalCurrency;
        /**
         *含税退货金额（本位币）
         */
        private BigDecimal taxReturnAmountLocalCurrency;
        /**
         *汇率
         */
        private BigDecimal exchangeRate;
    }


    @Data
    @NoArgsConstructor
    public static class InfoDTO {


        private String id;


        /**
         * 主表id
         */
        private String mainId;

        /**
         * skuid
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 发货状态
         * completeShipment 已发货
         * unShipped 未发货
         * partialShipment 部分发货
         */
        private String deliveryStatus;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

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
         * 备注
         */
        private String remark;

        private String approveStatus;

    }

    @Data
    @NoArgsConstructor
    public static class TypeCountDTO {
        /**
         * 类型
         */
        private String type;

        /**
         * 数量
         */
        private Integer count;
    }


    /**
     * 销售订单明细
     * 导出成功数据
     */
    @Data
    @NoArgsConstructor
    public static class ExcelDTO {

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
         * 销售数量
         */
        private Integer qty;


        /**
         * 单价
         */
        private BigDecimal price;


        /**
         * 税率
         */
        private BigDecimal taxRate;


        /**
         * 单位
         */
        private String unit;


        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否补发
         */
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;


        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 采购合同明细
     */
    @Data
    @NoArgsConstructor
    public static class ExportPdfDTO {


        /**
         * 序号
         */
        private Integer no;

        private String skuId;

        /**
         * 物料编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;


        /**
         * 型号
         */
        private String declareModel;


        /**
         * 数量
         */
        private Integer qty;

        /**
         * 单位
         */
        private String unit;
        /**
         * 销售单价(不含税单价)
         */
        private BigDecimal price;
        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * 税率
         */
        private BigDecimal taxRate;


        /**
         * 金额 = 单价 * 数量
         */
        private BigDecimal amount;

        /**
         * 含税金额
         */
        private BigDecimal taxAmount;
    }


    /**
     * 修改发货状态
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDeliveryStatusDTO {


        private String id;


        /**
         * 发货数量
         */
        private Integer deliveryQty;

    }

    /**
     * 明细
     */
    @Data
    @NoArgsConstructor
    public static class CalDetailDTO {

        /**
         * 序号
         */
        @NotNull(message = "序号不能为空")
        private Integer index;

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * sku编号
         */
        @NotBlank(message = "sku不能为空")
        private String skuNo;


        /**
         * 销售数量
         */
        @NotNull(message = "销售数量不能为空")
        @DecimalMax(value = "999999999", message = "销售数量最大值")
        @Min(value = 0, message = "销售数量最小值小于0")
        private Integer qty;

        /**
         * 单价
         */
        @NotNull(message = "销售单价不能为空")
        private BigDecimal price;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 是否赠品
         */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift;

    }


    /**
     * 明细
     */
    @Data
    @NoArgsConstructor
    public static class CalDetailResultDTO {

        /**
         * 序号
         */
        private Integer index;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 币种
         */
        private String currency;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 含税单价本位币
         */
        private BigDecimal taxPriceLc;

        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 金额含税（折扣后）
         */
        private BigDecimal taxAmount;

        /**
         * 金额含税（折扣前）
         */
        private BigDecimal taxAmountBefore;

        /**
         * 折扣额
         */
        private BigDecimal discountAmount;


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
         * 本位币的计算汇率值
         */
        private BigDecimal exchangeRate;

        /**
         * 价税合计额（折后）本位币
         */
        private BigDecimal allAmountLocalCurrency;

        /**
         * 销售金额（折后）本位币
         */
        private BigDecimal amountLocalCurrency;

    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateFrozenQtyDTO {

        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;

        /**
         * 冻结数量
         */
        @NotBlank(message = "冻结数量不能为空")
        private Integer frozenQty;
    }
}
