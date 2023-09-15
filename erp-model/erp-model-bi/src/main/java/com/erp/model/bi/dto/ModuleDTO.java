package com.erp.model.bi.dto;

import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 模块
 *
 * @Classname ModuleDTO

 * @Date 2022-12-12 9:46
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ModuleDTO implements Serializable {


    /**
     * 表id
     */
    @NotBlank(message = "id不能为空",  groups = {UpdateGroup.class} )
    private String id;

    /**
     *系统模块id
     */
    private String sysModuleId;

    /**
     * 分类id
     * 来源于字典表
     */
    private String categoryId;


    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空")
    //@Size(max = 30, message = "最大30字符")
    private String name;


    /**
     * 模块说明
     */
   // @Size(max = 200, message = "最大200字符")
    private String remark;


    /**
     * 图片地址
     */
    private Object imageFile=null;

    /**
     * 图片地址
     */
    private String imageUrl;


    /**
     * 编辑的上传表示
     * 如果为true 表示
     * 改变了  如果为false 就是没有
     */
    @NotNull(message = "上传标识不能为空",  groups = {UpdateGroup.class} )
    private Boolean uploadFlag=false;


    /**
     * 前端组件名不能为空
     */
    @NotBlank(message = "前端组件名不能为空")
    private String viewCode;

    @NotBlank(message = "编码不能为空")
    private String code;

    /**
     * 分享的ID, 用户ID/角色ID
     */
    @Deprecated
    private List<String> shareFlagIdList;

    /**
     * 分享标示
     * personal 私人
     * share 按多用户ID共享
     * role 按多角色ID
     */
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
    private String shareFlag = "personal";

    /**
     * 分享的ID, 用户ID/角色ID
     */
    private List<String> permissionUserIdList;
}
