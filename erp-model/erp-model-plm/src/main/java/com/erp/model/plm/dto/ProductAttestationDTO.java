package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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


    private String skuNo;



    /**
     * 产品认证
     */
    private List<AttestationDTO> productList;



    /**
     * 运输认证
     */
    private List<AttestationDTO> transportList;



    /**
     * 其它 认证
     */
    private List<AttestationDTO> otherList;
}
