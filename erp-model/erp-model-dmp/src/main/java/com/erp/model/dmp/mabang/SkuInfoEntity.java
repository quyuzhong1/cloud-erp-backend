package com.erp.model.dmp.mabang;

import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.mabang.item.MachiningDto;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SkuInfoEntity extends CleanBaseDTO {
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
     * 日均销量
     */
    private String forecastDaySale;
    /**
     * 是否含电池:1是;2否
     */
    private Integer hasBattery;
    /**
     * 是否新款:1:是;2否
     */
    private Integer isNewType;
    /**
     * 是否侵权:1是;2否
     */
    private Integer isTort;
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
     * 页面显示图片地址
     */
    private String stockPicture;
    /**
     * 原始图片地址
     */
    private String salePicture;
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
     * 重量
     */
    private BigDecimal weight;
    /**
     * 申报品名(中文)
     */
    private String declareName;
    /**
     * 申报品名(英文)
     */
    private String declareEname;
    /**
     * 备注
     */
    private String remark;
    /**
     * 销售备注
     */
    private String saleRemark;
    /**
     * 采购备注
     */
    private String purchaseRemark;
    /**
     * 包材
     */
    @SerializedName("package")
    private String packageX;
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
     * 开发员ID
     */
    private String developerId;
    /**
     * 开发员名称
     */
    private String developerName;
    /**
     * 采购员ID
     */
    private Integer buyerId;
    /**
     * 采购员名称
     */
    private String buyerName;
    /**
     * 美工ID
     */
    private Integer artDesignerId;
    /**
     * 美工名称
     */
    private String artDesignerName;
    /**
     * 默认供应商名称,接口参数传showProvider才返回
     */
    private String provider;
    /**
     * 采购链接,接口参数传showProvider才返回
     */
    private String productLinkAddress;
    /**
     * 财务编号
     */
    private String financial;
    /**
     * 商品用途
     */
    private String commodityUse;
    /**
     * 商品材质
     */
    private String commodityMaterial;
    /**
     * 库存详情图片
     */
    private List<?> stockDetailImg;
    /**
     * 销售信息
     */
    private List<?> sales;
    /**
     * 虚拟sku
     */
    private List<?> virtualSku;
    /**
     * 仓库信息
     */
    private List<?> warehouse;
    /**
     * 标签信息
     */
    private List<?> label;
    /**
     * 属性信息
     */
    private List<?> attributes;
    /**
     * 是否有加工产品:1有,2无
     */
    private Integer isMachining;
    /**
     * 加工费用
     */
    private BigDecimal processCost;
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
    /**
     * 打包长
     */
    @SerializedName("packaging_length")
    private BigDecimal packagingLength;
    /**
     * 打包宽
     */
    @SerializedName("packaging_width")
    private BigDecimal packagingWidth;
    /**
     * 打包高
     */
    @SerializedName("packaging_height")
    private BigDecimal packagingHeight;
    @Override
    public String toString() {
        return "SkuInfoEntity{" +
                "id='" + id + '\'' +
                ", salesSku='" + salesSku + '\'' +
                ", stockSku='" + stockSku + '\'' +
                ", nameCN='" + nameCN + '\'' +
                ", nameEN='" + nameEN + '\'' +
                ", defaultCost=" + defaultCost +
                ", forecastDaySale='" + forecastDaySale + '\'' +
                ", hasBattery=" + hasBattery +
                ", isNewType=" + isNewType +
                ", isTort=" + isTort +
                ", livenessType=" + livenessType +
                ", status=" + status +
                ", timeCreated='" + timeCreated + '\'' +
                ", timeModify='" + timeModify + '\'' +
                ", originalSku='" + originalSku + '\'' +
                ", brandName='" + brandName + '\'' +
                ", parentCategoryName='" + parentCategoryName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", thirdCategoryName='" + thirdCategoryName + '\'' +
                ", salePrice=" + salePrice +
                ", declareValue=" + declareValue +
                ", stockPicture='" + stockPicture + '\'' +
                ", salePicture='" + salePicture + '\'' +
                ", length=" + length +
                ", width=" + width +
                ", height=" + height +
                ", weight=" + weight +
                ", declareName='" + declareName + '\'' +
                ", declareEname='" + declareEname + '\'' +
                ", remark='" + remark + '\'' +
                ", saleRemark='" + saleRemark + '\'' +
                ", purchaseRemark='" + purchaseRemark + '\'' +
                ", packageX='" + packageX + '\'' +
                ", declareCode='" + declareCode + '\'' +
                ", isGift=" + isGift +
                ", magnetic=" + magnetic +
                ", powder=" + powder +
                ", purchasePrice='" + purchasePrice + '\'' +
                ", developerId='" + developerId + '\'' +
                ", developerName='" + developerName + '\'' +
                ", buyerId=" + buyerId +
                ", buyerName='" + buyerName + '\'' +
                ", artDesignerId=" + artDesignerId +
                ", artDesignerName='" + artDesignerName + '\'' +
                ", provider='" + provider + '\'' +
                ", productLinkAddress='" + productLinkAddress + '\'' +
                ", financial='" + financial + '\'' +
                ", commodityUse='" + commodityUse + '\'' +
                ", commodityMaterial='" + commodityMaterial + '\'' +
                ", stockDetailImg=" + stockDetailImg +
                ", sales=" + sales +
                ", virtualSku=" + virtualSku +
                ", warehouse=" + warehouse +
                ", label=" + label +
                ", attributes=" + attributes +
                ", isMachining=" + isMachining +
                ", processCost=" + processCost +
                ", stocksku='" + stocksku + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", quantity='" + quantity + '\'' +
                ", machiningData=" + machiningData +
                ", isPaste=" + isPaste +
                ", chargedProperty='" + chargedProperty + '\'' +
                ", packagingLength=" + packagingLength +
                ", packagingWidth=" + packagingWidth +
                ", packagingHeight=" + packagingHeight +
                '}';
    }
}
