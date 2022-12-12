package com.erp.model.bi.dto;

import com.erp.common.modules.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

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
public class ModuleSysDTO implements Serializable {


    /**
     * 表id
     */
    @NotBlank(message = "id不能为空",  groups = {UpdateGroup.class} )
    private String id;

    /**
     * 父级id
     * 默认为0
     */
    private String pid;


    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空")
    @Size(max = 30, message = "最大30字符")
    private String name;



}
