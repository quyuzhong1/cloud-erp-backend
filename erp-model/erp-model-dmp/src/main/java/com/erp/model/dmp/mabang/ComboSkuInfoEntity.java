package com.erp.model.dmp.mabang;

import com.erp.model.dmp.dto.CleanBaseDTO;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 组合商品信息
 * @author Cloud
 */
@Data
@NoArgsConstructor
public class ComboSkuInfoEntity extends CleanBaseDTO {

    private String _id;
    /**
     * 组合SKU
     */
    private String comboSku;
    /**
     * 组合SKU名称
     */
    private String name;
    /**
     * 组合SKU英文名称
     */
    private String nameEn;
    /**
     * 组合商品图片
     */
    private String comboPicture;
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
     * 包材
     */
    @SerializedName("package")
    private String packageX;
    /**
     * 包装个数
     */
    private Integer packageQuantity;
    /**
     * 申报品名(中文)
     */
    private String declareName;
    /**
     * 申报品名(英文)
     */
    private String declareEname;
    /**
     * 申报价格
     */
    private BigDecimal declareFee;
    /**
     * 重量
     */
    private BigDecimal declareWeight;
    /**
     * 报关编码
     */
    private String declareCustoms;

    /**
     * 组合商品信息
     */
    private List<ComboProductDetail> comboProductDetail;
    /**
     * 虚拟sku
     */
    private List<?> virtualSku;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 关系类型 组合：combine 加工：machining
     */
    private String relationType;

    /**
     * 平台标识 管易云，马帮
     */
    private String platformSign;


    @Override
    public String toString() {
        return "ComboSkuInfoEntity{" +
                ", comboSku='" + comboSku + '\'' +
                ", name='" + name + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", comboPicture='" + comboPicture + '\'' +
                ", length=" + length +
                ", width=" + width +
                ", height=" + height +
                ", packageX='" + packageX + '\'' +
                ", packageQuantity=" + packageQuantity +
                ", declareName='" + declareName + '\'' +
                ", declareEname='" + declareEname + '\'' +
                ", declareFee=" + declareFee +
                ", declareWeight=" + declareWeight +
                ", declareCustoms='" + declareCustoms + '\'' +
                ", comboProductDetail=" + comboProductDetail +
                '}';
    }


    @Data
    @ToString
    public static class ComboProductDetail{
        /**
         * 库存SKU
         */
        private String stockSku;
        /**
         * 组合数量
         */
        private Integer quantity;
        /**
         * 组合商品名称
         */
        private String nameCN;
    }
}
