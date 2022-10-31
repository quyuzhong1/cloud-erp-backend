package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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
     * 用户id
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;


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


    /**
     * 是否是项目负责人
     * 0不是 1 是
     */
    @StateEnumValue(intValues = {0, 1}, message = "是否是项目负责人")
    private Integer isCharge;
}
