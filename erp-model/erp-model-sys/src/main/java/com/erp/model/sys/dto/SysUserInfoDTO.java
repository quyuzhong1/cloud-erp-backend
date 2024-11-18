package com.erp.model.sys.dto;


import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname 系统用户入参

 * @Date 2022-07-08 9:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserInfoDTO implements Serializable {

    //用户id
    private String uid;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 1,max = 25,message = "用户名最大长度为25,最小长度为1")
    private String userName;

    private String realName;

    @Size(min = 3,max = 50, message = "邮箱最大50字符,最小长度为3")
    @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MAILBOX,message = "邮箱格式有误")
    private String email;

    //电话
    @NotBlank(message = "电话不能为空")
    @Size(min = 7,max = 20, message = "电话最大20字符,最小长度为7")
    @RegularValid(formatPattern= FieldFormatPatternTypeEnum.MOBILE,message = "电话格式有误")
    private String mobile;

    //o 禁用 1 正常
    private Integer userState;

    //0 自动生成
    private Integer createPasswordType;

    //@NotBlank(message = "密码不能为空")
    private String password;

   // @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    /**
     * 用户类型 erp srm
     */
    private String userType ;
    /**
     *是否强制登录 修改密码
     */
    private Boolean needChangePwd;
    /**
     * 是否超级管理员 false 不是管理员
     */
    private Boolean isSuper;
    /**
     * 供应商id
     */
    private String supplierId;
    /**
     * 供应商用户关系id
     */
    private String refId;
    //角色id 集合
    private List<String> roleIdList;

    /**
     * 店铺权限设置分页查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ShopAuthPagingSearchDTO {

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 真实名称
         */
        private String realName;

        /**
         * 店铺id集合 http://172.16.100.11:3002/project/110/interface/api/22975
         */
        private List<String> shopIdList;

        /**
         * 状态
         */
        private Integer userState;

        /**
         * 角色id集合
         */
        private List<String> roleIdList;

        /**
         * 部门id
         */
        private List<String> deptIdList;

        /**
         * 更新人id集合
         */
        private List<String> updateUserIdList;

        /**
         * 更新时间
         */
        private List<LocalDate> updateTimeList;

    }

    /**
     * 店铺权限设置分页查询显示
     */
    @Data
    @NoArgsConstructor
    public static class ShopAuthPagingDTO {
        /**
         * 用户id
         */
        private String userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * o 禁用 1 启用
         */
        private Integer userState;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 店铺
         */
        private String shopNames;
    }
}
