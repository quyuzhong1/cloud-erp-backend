package com.erp.model.oms.dto;

import com.common.business.validator.AddGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname SoInfoDTO
 * @Description TODO
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
        private Integer qty;

        /**
         * 单价
         */
        @NotNull(message = "销售单价不能为空", groups = {AddGroup.class})
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
         * 备注
         */
        @Size(max = 200, message = "备注最大200字符")
        private String remark;


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
         * 主表id
         */
        private String mainId;
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
         * 仓库id
         */
        private String warehouseId;
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
         * true 已发货
         * false 未发货
         */
        private Boolean deliveryStatus;

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
         * 合计
         */
        private BigDecimal amount;
    }
}
