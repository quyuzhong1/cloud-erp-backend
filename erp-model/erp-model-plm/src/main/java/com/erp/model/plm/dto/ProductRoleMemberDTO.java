package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
     * 成员id
     */
    private String memberId;

    /**
     * 成员名
     */
    private String memberName;

    /**
     * 产品数量
     */
    private String productCount ;
}
