package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
     * 辅料的sku id
     */
    private String accessoriesSkuId;

    /**
     * 数量
     */
    private Integer quantity;


}
