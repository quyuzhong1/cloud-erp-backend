package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.naming.directory.SearchResult;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname ProductRoleDTO
 * @Description TODO
 * @Date 2022-10-09 19:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductRoleDTO implements Serializable {


    /**
     * 产品角色id
     */
    private String productRoleId;

    /**
     * 产品角色名
     */
    private String productRoleName;


    /**
     * 产品角色下 成员数
     */
    private List<ProductRoleMemberDTO> productRoleMembers;
}
