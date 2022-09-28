package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname MemberPagingDTO
 * @Description TODO
 * @Date 2022-09-26 18:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class MemberPagingDTO  implements Serializable {

    @NotBlank(message = "项目id 不能为空")
    private String projectId;

    //项目角色id
    private String projectRoleId;
}
