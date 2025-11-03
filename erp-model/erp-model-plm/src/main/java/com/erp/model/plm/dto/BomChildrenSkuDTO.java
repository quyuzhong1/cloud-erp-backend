package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname BomChildrenSkuDTO

 * @Date 2023-02-14 19:36
 * @Created by yl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomChildrenSkuDTO  implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * bomId
     */
    private String bomId;


    /**
     * 类型combination 组合 single 单品
     */
    private String type;

    /**
     * bom 编号
     */
    private String serialNumber;

    /**
     * bom历史id
     */
    private String bomHistoryId;
    /**
     * bom版本
     */
    private String bomVersion;

    /**
     * 父级skuId
     */
    private String parentSkuId;

    /**
     * 父级skuNo
     */
    private String parentSkuNo;

    /**
     * sku
     */
    @NotBlank(message = "sku不能为空")
    private String skuNo;


    private String skuId;


    private String productId;

    /**
     * 产品尺寸长
     */
    private BigDecimal length;
    /**
     * 产品尺寸宽
     */
    private BigDecimal width;
    /**
     * 产品尺寸高
     */
    private BigDecimal height;
    /**
     * 毛重
     */
    private BigDecimal grossWeight;
    /**
     * 净重
     */
    private BigDecimal netWeight;
    /**
     * sku名称
     */
    private String skuName;

    /**
     * 单位
     */
    private String unitName;

    /**
     * 层级
     */
    private Integer level;


    /**
     * 图片路径
     */
    private String imageUrl;
    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @DecimalMax(value = "9999",message ="最大值为9999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Integer quantity;

    /**
     * 子件虚拟仓可用库存
     */
    private Integer virtualUsableQty;

    /**
     * 子件实体仓可用库存
     */
    private Integer warehouseUsableQty;

}
