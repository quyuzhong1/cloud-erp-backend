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


    private String id;

    @NotBlank(message = "用户id不能为空")
    private String userId;

    @NotBlank(message = "用户名不能为空")
    private String useName;

    @NotBlank(message = "产品id不能为空")
    private String productId;

    @NotBlank(message = "项目id不能为空")
    private String projectId;

    @NotBlank(message = "项目角色id 不能为空")
    private String roleId;


    @StateEnumValue(intValues = {0, 1}, message = "是否是项目负责人")
    private Integer isCharge;
}
