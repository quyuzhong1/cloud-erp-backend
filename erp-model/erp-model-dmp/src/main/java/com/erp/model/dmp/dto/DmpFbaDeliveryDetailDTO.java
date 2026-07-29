package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@NoArgsConstructor
public class DmpFbaDeliveryDetailDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * MSKUID
        */
        private String fbastockId;

        /**
        * 货件id
        */
        private String shippBatchnewId;

        /**
        * MSKU
        */
        private String platformSku;

        /**
        * 商品id
        */
        private String stockId;

        /**
        * 货件号
        */
        private String shippNo;

        /**
        * 重量
        */
        private BigDecimal weight;

        /**
        * 发货数量
        */
        private Integer deliveryNum;

        /**
        * 已发货数量
        */
        private Integer useDeliveryNum;

        /**
        * 发货单详情备注
        */
        private String remark;

        /**
        * 成本
        */
        private BigDecimal cost;

        /**
        * 企业编号
        */
        private String companyId;

        /**
        * fba仓库id
        */
        private String fbaWarehouseId;

        /**
        * 国家
        */
        private String amazonsite;

        /**
        * 体积
        */
        private BigDecimal volume;

        /**
        * 分摊物流费用
        */
        private BigDecimal logicComputeCost;

        /**
        * 分摊自定义费用
        */
        private BigDecimal customComputeCost;

        /**
        * 已经与入库队列表关联的数量
        */
        private Integer sharedQuantity;

        /**
        * 销售员id 多个销售员逗号隔开
        */
        private String saleId;

        /**
        * 包材费
        */
        private BigDecimal packageCost;

        /**
        * 库存锁定状态 1无需锁定 2锁定中 3 锁定成功 4部分成功 5锁定失败
        */
        private Integer lockState;

        /**
        * 锁定数量
        */
        private Integer lockQty;

        /**
        * 锁定时间
        */
        private String lockTime;

        /**
        * 失败或成功备注
        */
        private String lockRemark;

        /**
        * 0锁定 默认值0 每次更新+1
        */
        private Integer lockVersion;

        /**
        * 销售名称
        */
        private String salename;

        /**
        * 包装类型
        */
        private String packtype;

        /**
        * asin
        */
        private String asin;

        /**
        * msku
        */
        private String msku;

        /**
        * 关联量
        */
        private Integer correlationNum;

        /**
        * 申报量
        */
        private Integer applyQuantity;

        /**
        * 货件状态
        */
        private String shipmentStatus;

        /**
        * 物流中心编码
        */
        private String logisticsCode;

        /**
        * 品名
        */
        private String stockName;

        /**
        * 图片
        */
        private String pictururl;

        /**
        * fnsku
        */
        private String fnsku;

        /**
        * 商品SKU
        */
        private String sku;

        /**
        * 国家
        */
        private String state;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 主单id
        */
        private String mainId;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * MSKUID
        */
        @NotBlank(message = "MSKUID不能为空")
        @Size(max = 64,message = "MSKUID最大长度不能超过64位")
        private String fbastockId;

        /**
        * 货件id
        */
        @NotBlank(message = "货件id不能为空")
        @Size(max = 64,message = "货件id最大长度不能超过64位")
        private String shippBatchnewId;

        /**
        * MSKU
        */
        @NotBlank(message = "MSKU不能为空")
        @Size(max = 64,message = "MSKU最大长度不能超过64位")
        private String platformSku;

        /**
        * 商品id
        */
        @NotBlank(message = "商品id不能为空")
        @Size(max = 64,message = "商品id最大长度不能超过64位")
        private String stockId;

        /**
        * 货件号
        */
        @NotBlank(message = "货件号不能为空")
        @Size(max = 64,message = "货件号最大长度不能超过64位")
        private String shippNo;

        /**
        * 重量
        */
        @NotNull(message = "重量不能为空")
        @Digits(integer = 18, fraction = 6, message = "重量整数位不能超过18位，小数位不能超过6位")
        private BigDecimal weight;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryNum;

        /**
        * 已发货数量
        */
        @NotNull(message = "已发货数量不能为空")
        private Integer useDeliveryNum;

        /**
        * 发货单详情备注
        */
        @NotBlank(message = "发货单详情备注不能为空")
        @Size(max = 255,message = "发货单详情备注最大长度不能超过255位")
        private String remark;

        /**
        * 成本
        */
        @NotNull(message = "成本不能为空")
        @Digits(integer = 18, fraction = 6, message = "成本整数位不能超过18位，小数位不能超过6位")
        private BigDecimal cost;

        /**
        * 企业编号
        */
        @NotBlank(message = "企业编号不能为空")
        @Size(max = 64,message = "企业编号最大长度不能超过64位")
        private String companyId;

        /**
        * fba仓库id
        */
        @NotBlank(message = "fba仓库id不能为空")
        @Size(max = 64,message = "fba仓库id最大长度不能超过64位")
        private String fbaWarehouseId;

        /**
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        @Size(max = 64,message = "国家最大长度不能超过64位")
        private String amazonsite;

        /**
        * 体积
        */
        @NotNull(message = "体积不能为空")
        @Digits(integer = 18, fraction = 6, message = "体积整数位不能超过18位，小数位不能超过6位")
        private BigDecimal volume;

        /**
        * 分摊物流费用
        */
        @NotNull(message = "分摊物流费用不能为空")
        @Digits(integer = 18, fraction = 6, message = "分摊物流费用整数位不能超过18位，小数位不能超过6位")
        private BigDecimal logicComputeCost;

        /**
        * 分摊自定义费用
        */
        @NotNull(message = "分摊自定义费用不能为空")
        @Digits(integer = 18, fraction = 6, message = "分摊自定义费用整数位不能超过18位，小数位不能超过6位")
        private BigDecimal customComputeCost;

        /**
        * 已经与入库队列表关联的数量
        */
        @NotNull(message = "已经与入库队列表关联的数量不能为空")
        private Integer sharedQuantity;

        /**
        * 销售员id 多个销售员逗号隔开
        */
        @NotBlank(message = "销售员id 多个销售员逗号隔开不能为空")
        @Size(max = 255,message = "销售员id 多个销售员逗号隔开最大长度不能超过255位")
        private String saleId;

        /**
        * 包材费
        */
        @NotNull(message = "包材费不能为空")
        private BigDecimal packageCost;

        /**
        * 库存锁定状态 1无需锁定 2锁定中 3 锁定成功 4部分成功 5锁定失败
        */
        @NotNull(message = "库存锁定状态 1无需锁定 2锁定中 3 锁定成功 4部分成功 5锁定失败不能为空")
        private Integer lockState;

        /**
        * 锁定数量
        */
        @NotNull(message = "锁定数量不能为空")
        private Integer lockQty;

        /**
        * 锁定时间
        */
        @NotBlank(message = "锁定时间不能为空")
        @Size(max = 64,message = "锁定时间最大长度不能超过64位")
        private String lockTime;

        /**
        * 失败或成功备注
        */
        @NotBlank(message = "失败或成功备注不能为空")
        @Size(max = 64,message = "失败或成功备注最大长度不能超过64位")
        private String lockRemark;

        /**
        * 0锁定 默认值0 每次更新+1
        */
        @NotNull(message = "0锁定 默认值0 每次更新+1不能为空")
        private Integer lockVersion;

        /**
        * 销售名称
        */
        @NotBlank(message = "销售名称不能为空")
        @Size(max = 64,message = "销售名称最大长度不能超过64位")
        private String salename;

        /**
        * 包装类型
        */
        @NotBlank(message = "包装类型不能为空")
        @Size(max = 64,message = "包装类型最大长度不能超过64位")
        private String packtype;

        /**
        * asin
        */
        @NotBlank(message = "asin不能为空")
        @Size(max = 64,message = "asin最大长度不能超过64位")
        private String asin;

        /**
        * msku
        */
        @NotBlank(message = "msku不能为空")
        @Size(max = 64,message = "msku最大长度不能超过64位")
        private String msku;

        /**
        * 关联量
        */
        @NotNull(message = "关联量不能为空")
        private Integer correlationNum;

        /**
        * 申报量
        */
        @NotNull(message = "申报量不能为空")
        private Integer applyQuantity;

        /**
        * 货件状态
        */
        @NotBlank(message = "货件状态不能为空")
        @Size(max = 64,message = "货件状态最大长度不能超过64位")
        private String shipmentStatus;

        /**
        * 物流中心编码
        */
        @NotBlank(message = "物流中心编码不能为空")
        @Size(max = 64,message = "物流中心编码最大长度不能超过64位")
        private String logisticsCode;

        /**
        * 品名
        */
        @NotBlank(message = "品名不能为空")
        @Size(max = 100,message = "品名最大长度不能超过100位")
        private String stockName;

        /**
        * 图片
        */
        @NotBlank(message = "图片不能为空")
        @Size(max = 255,message = "图片最大长度不能超过255位")
        private String pictururl;

        /**
        * fnsku
        */
        @NotBlank(message = "fnsku不能为空")
        @Size(max = 255,message = "fnsku最大长度不能超过255位")
        private String fnsku;

        /**
        * 商品SKU
        */
        @NotBlank(message = "商品SKU不能为空")
        @Size(max = 255,message = "商品SKU最大长度不能超过255位")
        private String sku;

        /**
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        @Size(max = 255,message = "国家最大长度不能超过255位")
        private String state;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 64,message = "店铺id最大长度不能超过64位")
        private String shopId;

        /**
        * 主单id
        */
        @NotBlank(message = "主单id不能为空")
        @Size(max = 19,message = "主单id最大长度不能超过19位")
        private String mainId;


    }


}