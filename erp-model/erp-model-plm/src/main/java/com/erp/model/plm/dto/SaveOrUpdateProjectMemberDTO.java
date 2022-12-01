package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname AddProjectMemberDTO
 * @Description TODO
 * @Date 2022-09-26 15:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SaveOrUpdateProjectMemberDTO implements Serializable {

    /**
     * 表id
     */
    private String id;

    /**
     * 关系表id
     */
    private String roleRefMemberId;

    /**
     * 用户id集合不能为空
     */
    @NotNull(message = "用户id不能为空")
    private List<String> userIdList;


    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;



    /**
     * 角色id
     */
    @NotBlank(message = "项目角色id 不能为空")
    private String roleId;



}
