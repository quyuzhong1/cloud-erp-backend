package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname RemoveProjectMemberDTO
 * @Description TODO
 * @Date 2022-10-11 11:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class RemoveProjectMemberDTO  implements Serializable {

    /**
     * 表id
     */
    @NotBlank(message = "表id 不能为空")
    private String id;

    /**
     * 关系表id
     */
    @NotBlank(message = "关系表id 不能为空")
    private String roleRefMemberId;
}
