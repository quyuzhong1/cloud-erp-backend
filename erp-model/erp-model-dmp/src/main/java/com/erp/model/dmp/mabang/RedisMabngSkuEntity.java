package com.erp.model.dmp.mabang;

import com.erp.model.dmp.mabang.item.MachiningDto;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class RedisMabngSkuEntity{
    /**
     * stockSkuId
     */
    private String id;
    /**
     * 主SKU
     */
    private String salesSku;
    /**
     * 库存SKU
     */
    private String stockSku;
    /**
     * 中文名
     */
    private String nameCN;
    /**
     * 英文名
     */
    private String nameEN;
    /**
     * 统一成本价
     */
    private BigDecimal defaultCost;
    /**
     * 活跃度:1:爆款;2:旺款;3:平款;4:滞销款
     */
    private Integer livenessType;
    /**
     * 状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
     */
    private Integer status;
    /**
     * 创建时间
     */
    private String timeCreated;
    /**
     * 更新时间
     */
    private String timeModify;
    /**
     * 原厂SKU
     */
    private String originalSku;
    /**
     * 品牌
     */
    private String brandName;
    /**
     * 商品目录(一级)
     */
    private String parentCategoryName;
    /**
     * 商品目录(二级)
     */
    private String categoryName;
    /**
     * 商品目录(三级)
     */
    private String thirdCategoryName;
    /**
     * 售价
     */
    private BigDecimal salePrice;
    /**
     * 申报价格
     */
    private BigDecimal declareValue;
    /**
     * 申报品名(中文)
     */
    private String declareName;
    /**
     * 申报品名(英文)
     */
    private String declareEname;
    /**
     * 报关编码
     */
    private String declareCode;
    /**
     * 是否赠品1是;2否
     */
    private Integer isGift;
    /**
     * 带磁:1:是;2:否
     */
    private Integer magnetic;
    /**
     * 粉末:1:是;2:否
     */
    private Integer powder;
    /**
     * 最新采购价"
     */
    private String purchasePrice;
    /**
     * 采购员ID
     */
    private Integer buyerId;
    /**
     * 采购员名称
     */
    private String buyerName;
    /**
     * 财务编号
     */
    private String financial;
    /**
     * 原材料SKU
     */
    private String stocksku;
    /**
     * 原材料默认仓库编号
     */
    private String warehouseId;
    /**
     * 原材料默认仓库名称
     */
    private String warehouseName;
    /**
     * 原材料配比数量
     */
    private String quantity;
    /**
     * 加工产品信息
     */
    @SerializedName("MachiningData")
    private List<MachiningDto> machiningData;
    /**
     * 属性变更
     */
    @SerializedName("ispaste")
    private Integer isPaste;
    /**
     * 变更属性
     */
    @SerializedName("chargedproperty")
    private String chargedProperty;
}
