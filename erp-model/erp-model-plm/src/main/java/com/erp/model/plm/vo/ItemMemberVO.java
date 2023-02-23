package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ItemMemberVO
 * @Description TODO
 * @Date 2023-02-23 14:51
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ItemMemberVO implements Serializable {


    /**
     * 产品id
     */
    private String productId;

    /**
     * 成员id
     */
    private String memberId;

    /**
     * 成员名
     */
    private String memberName;


    /**
     * 角色id
     */
    private String roleId;

    /**
     * 角色名
     */
    private String roleName;
}
