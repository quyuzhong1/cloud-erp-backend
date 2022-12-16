package com.erp.model.bi.dto;

import com.erp.common.modules.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 模块
 *
 * @Classname ModuleDTO
 * @Description TODO
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
    @Size(max = 30, message = "最大30字符")
    private String name;


    /**
     * 模块说明
     */
    @Size(max = 200, message = "最大200字符")
    private String remark;


    /**
     * 图片地址
     */
    private MultipartFile imageFile;

    /**
     * 图片地址
     */
    private String imageUrl;


    /**
     * 前端组件名不能为空
     */
    @NotBlank(message = "前端组件名不能为空")
    private String viewCode;


    /**
     * 权限人员
     */
    private List<String> permissionUserIdList;
}
