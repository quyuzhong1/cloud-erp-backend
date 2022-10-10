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
public class saveOrUpdateProjectMemberDTO implements Serializable {

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
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String useName;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;

    /**
     * 项目id
     */
    @NotBlank(message = "项目id不能为空")
    private String projectId;

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
