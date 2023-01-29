package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 产品信息的最小拆分的信息
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-29 11:12
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductSmallestUnitDTO implements Serializable {


    /**
     *  spu 信息
     */
    private SpuDTO spuInfo;


    private SkuDTO skuInfo;
}
