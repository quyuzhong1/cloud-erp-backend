package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 包装辅料信息
 * @Classname
 * @Description TODO
 * @Date 2023-02-25 13:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductAccessoriesDTO implements Serializable {


    /**
     * 主键id
     */
    private String id;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 父级skuid
     */
    private String parentSkuId;

    /**
     * 父级sku no
     */
    private String parentSkuNo;



    /**
     * 父级sku 图片
     */
    private String parentSkuImagesUrl;


    /**
     * 辅料的sku id
     */
    private String accessoriesSkuId;


    /**
     * 辅料的sku no
     */
    private String accessoriesSkuNo;


    /**
     * 辅料的sku的名字
     */
    private String accessoriesSkuName;


    /**
     * 辅料的sku的图片
     */
    private String accessoriesSkuImagesUrl;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @DecimalMax(value = "9999",message ="最大值为9999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Integer quantity;


    /**
     *禁止修改的字段
     */
    private List<String> disableFieldList=new ArrayList<>();


}
