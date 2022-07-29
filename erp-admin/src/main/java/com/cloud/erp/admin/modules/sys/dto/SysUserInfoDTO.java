package com.cloud.erp.admin.modules.sys.dto;

import com.erp.common.validator.AddGroup;
import com.erp.common.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname 系统用户入参
 * @Description TODO
 * @Date 2022-07-08 9:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserInfoDTO implements Serializable {

    //用户id
    private String uid;

    @NotBlank(message = "用户名不能为空",  groups = {AddGroup.class})
    @Size(max = 25,message = "最大长度为25")
    private String userName;

    @NotBlank(message = "真实名不能为空",groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 10,message = "最大长度为10")
    private String realName;

    //电话
    @NotBlank(message = "电话不能为空")
    private String mobile;

    //o 禁用 1 正常
    private Integer userState;

    //0 自动生成
    private Integer createPasswordType;

    //@NotBlank(message = "密码不能为空")
    private String password;

   // @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    //角色id 集合
    private List<String> roleIdList;


}
