package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname ProductRoleMemberDTO
 * @Description TODO
 * @Date 2022-10-09 19:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductRoleMemberDTO implements Serializable {



    /**
     * 成员名
     */
    private String name;

    /**
     * 产品数量
     */
    private Integer count ;

    /**
     * 产品id 集合
     */
    private List<String> productIds ;
}
