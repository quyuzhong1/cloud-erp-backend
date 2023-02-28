package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
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


    private List<String> disableFieldList=new ArrayList<>();


    /**
     * 产品认证
     */
    private List<String> productList;



    /**
     * 运输认证
     */
    private List<String> transportList;



    /**
     * 其它 认证
     */
    private List<String> otherList;
}
