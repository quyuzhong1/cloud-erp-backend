package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * B2C销售订单明细表请求响应实体
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@NoArgsConstructor
public class SoB2cDetailDTO implements Serializable {

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
       private String id;
        /**
         * 图片
         */
       private String imageUrl;
        /**
         * 平台sku
         */
       private String platformSkuNo;
        /**
         * 平台产品id
         */
       private String platformSpuNo;
        /**
         * 产品skuId
         */
       private String skuId;
        /**
         * 产品sku
         */
       private String skuNo;
        /**
         * 产品名称
         */
       private String productName;
        /**
         * 规格属性
         */
       private String variantProperty;
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
        * 虚拟仓id
        */
       private String virtualWarehouseId;

        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;

        /**
         * 可用库存
         */
        private Integer virtualUsableQty;

        /**
         * 出货仓库
         */
       private String warehouseId;
        /**
         * 出货仓库
         */
       private String  warehouseName;
        /**
         * 仓位
         */
       private String warehouseLocation;
        /**
         * 可用库存
         */
       private Integer useableQty;
        /**
         * 冻结库存
         */
       private Integer freezeQty;
       /**
        * 明细标签
        */
       private String labelJson;
        /**
         * 扩展字段
         */
       private String extendData;

       /**
        * 来源平台（SoB2cSourcePlatformEnum枚举，selfAddERP新增，thirdPlatform第三方平台新增）
        */
       private String sourcePlatform;
        /**
         * 是否匹配仓库规则
         */
        private Boolean isMatchWarehouseRule;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 是否手工添加明细
         */
        private Boolean isSelfAdd;

        /**
        * 明细标签对象
        */
       private DetailLabelDTO detailLabelDTO;


        /**
         * 属性对象
         */
       private List<PropertyDTO> propertyDTOList;
        /**
         * 目的国申报价
         */
        private BigDecimal toDeclarePrice;

        /**
         * 目的国申报币种
         */
        private String toCurrency;

        /**
         * 目的国申报币种符号
         */
        private String toCurrencySymbol;

        /**
         * 申报标签(正常申报normal，高申报high，低申报low)
         */
        private String declareLabel;
        /**
         * 申报标签名称
         */
        private String declareLabelName;
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

        /**
         * 子件缺货信息
         */
        private List<SoB2cDTO.VirtualChildScarceDTO> childScarceList;

    }

    @Data
    @NoArgsConstructor
    public static class LabelJsonDTO {
        /**
         * 速卖通已税
         */
        private String alreadyTaxed;
        /**
         * 菜鸟官方仓
         */
        private String logisticsWarehouseType;
        /**
         * 速卖通打标
         */
        private List<String> tagList;
        /**
         * 当前明细是否退款: true=退款, false=未退款
         */
        private Boolean isRefunded;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyDTO {
        /**
         * 名称
         */
        private String name;

        /**
         * 备注
         */
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class DetailLabelDTO {

        /**
         * 速卖通已税(速卖通子订单already_taxed=U_TAXED或者I_TAXED打标)
         */
       private String alreadyTaxed;

        /**
         * 菜鸟官方仓(速卖通子订单logistics_warehouse_type=cainiaoInternationalWarehouse打标)
         */
       private String logisticsWarehouseType;

       /**
        * 1、AE_PLUS（速卖通子订单tags=AE_PLUS打标）
        * 2、AE_合单（速卖通子订单tags=HBA_UP_EXPRESS打标）
        * 3、十日达 （速卖通子订单tags=leadTimeTag#10打标）
        */
       private List<String> tagList;

        /**
         * 缺货订单(待审核、配货中订单，仓库可用库存为0)
         */
       private Boolean isOutStock;

       /**
        * 虚拟仓是否缺货
        */
       private Boolean isVirtualOutStock;

       /**
        * 当前是否退款: true=退款, false=未退款
        */
       private Boolean isRefunded;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO{

        /**
        * 主键id
        */
        private String  id;

        /**
        * 主表Id
        */
        private String  mainId;

        /**
        * 图片URL
        */
        private String imageUrl;

        /**
        * 平台sku
        */
        private String platformSkuNo;

        /**
        * 平台产品id
        */
        private String platformSpuNo;

        /**
         * 产品skuId
         */
        private String skuId;

        /**
        * 产品sku编号
        */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 产品尺寸（长）
         */
        private BigDecimal productLength;

        /**
         * 产品尺寸（宽）
         */
        private BigDecimal productWidth;

        /**
         * 产品尺寸（高）
         */
        private BigDecimal productHeight;
        /**
         * 毛重
         */
        private BigDecimal grossWeight;
        /**
         * 净重
         */
        private BigDecimal netWeight;

        /**
        * 库存sku编号 http://172.16.100.11:3002/project/110/interface/api/19609
        */
        private String warehouseSkuNo;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 建议售价（本位币）
        */
        private BigDecimal advicePrice;

        /**
        * 成本（本位币）
        */
        private BigDecimal taxCost;

        /**
        * 金额
        */
        private BigDecimal amount;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 来源详情id
        */
        private String sourceDetailId;

        /**
         * 拆分的明细id
         */
        private String splitDetailId;

        /**
         * 是否组合品
         */
        private Boolean isCombination = false;

        /**
         * 还原id
         */
        private String revertId;
        /**
         * 成本价格来源
         */
        private String costSource;
        //材料成本
        private BigDecimal productCost;
        //头程运费
        private BigDecimal firstMileShippingCost;
        //清关税费
        private BigDecimal clearanceCustomsTax;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 操作明细集合id
         */
        private String operateDetailId;

        /**
         * 拆分的Id 如果用户拆分BOM套装则这个值为原本的明细id
         */
        private String splitDetailId;

        /**
         * 平台明细行号
         */
        private String platformLineNumber;
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
         * 拆分的Id 如果用户拆分BOM套装则这个值为原本的明细id
         */
        private String splitDetailId;

        private String sourceDetailId;
        /**
         * 金额
         */
        private BigDecimal amount;

        /**
         * 建议售价（本位币）
         */
        private BigDecimal advicePrice;

        /**
         * 还原id
         */
        private String revertId;

        /**
         * 主表Id
         */
        private String mainId;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        @Min(value = 0,message = "数量最小值为0")
        @Max(value = 999999999,message = "数量最大值为999999999")
        private Integer qty;

        /**
        * 仓库id
        */
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 单价
        */
        @NotNull(message = "单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
         * 来源平台
         */
        private String sourcePlatform;

        private String warehouseSkuNo;
        /**
         * 平台sku编号
         */
        private String platformSkuNo;
        /**
         * 平台 产品id
         */
        private String platformSpuNo;

        /**
         * 含税成本（本位币）
         */
        private BigDecimal taxCost;
        /**
         * 成本价格来源
         */
        private String costSource;
        //材料成本
        private BigDecimal productCost;
        //头程运费
        private BigDecimal firstMileShippingCost;
        //清关税费
        private BigDecimal clearanceCustomsTax;
        /**
         * 是否赠品：true/false
         */
        private Boolean isGift;
        /**
         * 税率
         */
        private BigDecimal taxRate;
    }


    @Data
    @NoArgsConstructor
    public static class OutstockDTO{



        private String soDetailId;

        private String mianId;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku編號
         */
        private String skuNo;

        private Integer qty;

        private String warehouseLocation;

        private String warehouseId;

        private String warehouseName;

        private String platformSkuNo;

        private String platformSpuNo;

        private String warehouseSkuNo;

        private String warehouseOrgId;

        private String warehouseOrgName;

        private String remark;

        private String sourceDetailId;

        private String currency;

        private BigDecimal exchangeRate;

    }

    /**
     * 待发货查询参数DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaitDeliveryParamDTO{

        /**
         * skuId集合
         */
        private List<String> skuIdList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 销售订单明细id
         */
        private List<String> detailIdList;
    }

    /**
     * 待发货查询DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaitDeliveryQtyDTO{

        /**
         * skuId
         */
        private String skuId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 数量
         */
        private Integer qty;
    }
}