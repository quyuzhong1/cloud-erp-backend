package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**人员分类
 * @Classname ProductRoleDTO
 * @Description TODO
 * @Date 2022-10-09 19:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductRoleDTO implements Serializable {


    /**
     * 角色下的人数
     */
    private Integer count;

    /**
     * 产品角色名
     */
    private String name;


    /**
     * 产品角色下 成员 和对应的 产品数
     */
    private List<ProductRoleMemberDTO> productRoleMembers;
}
