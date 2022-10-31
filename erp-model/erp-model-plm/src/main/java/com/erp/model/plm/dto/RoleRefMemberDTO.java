package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname RoleRefMemberDTO
 * @Description TODO
 * @Date 2022-10-10 11:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class RoleRefMemberDTO  implements Serializable {

    /**
     * 成员id
     */
    private String membersId;

    /**
     * 角色id
     */
    private String roleId;
}
