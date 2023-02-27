package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 产品认证信息
 * @Classname
 * @Description TODO
 * @Date 2023-02-25 13:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductAttestationDTO implements Serializable {


    /**
     * SKUid
     */
    private String skuId;


    /**
     * c产品认证
     */
    private String product;

    /**
     * 运输认证
     */
    private String transport;


    /**
     * 其它 认证认证
     */
    private String other;
}
