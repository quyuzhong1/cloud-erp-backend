package com.cloud.erp.admin.modules.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SysUserBaseVO
 * @Description TODO
 * @Date 2022-08-03 10:27
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserBaseVO  implements Serializable {


    /**
     * 用户名
     */
    private String userName;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 电话号码
     */
    private String mobile;

    private String email;

    /**
     * 第三方平台类型
     */
    private String thirdPartyType;

}
