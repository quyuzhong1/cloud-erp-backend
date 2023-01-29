package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname
 * @Description TODO
 * @Date 2023-01-29 11:26
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuDTO  implements Serializable {


    /**
     * 产品图片
     */
    private String imagesUrl;


    /**
     * skuNo
     */
    private String skuNo;


    /**
     * SKU名称
     */
    private String name;



    /**
     * 单位表id
     */
    private String unitId;

    /**
     * 单位表名称
     */
    private String unitName;



    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private String productState;



}
