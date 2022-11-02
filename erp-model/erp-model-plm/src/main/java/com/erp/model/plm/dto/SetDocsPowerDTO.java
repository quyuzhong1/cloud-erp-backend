package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname 设置文档权限
 * @Description TODO
 * @Date 2022-09-23 14:54
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SetDocsPowerDTO implements Serializable {


    /**
     * 文档id
     */
    @NotBlank(message = "文档id不能为空")
    private String id;

    /**
     * 角色id
     * 传空就是全部
     */
    private List<String> roleIdList;
}
