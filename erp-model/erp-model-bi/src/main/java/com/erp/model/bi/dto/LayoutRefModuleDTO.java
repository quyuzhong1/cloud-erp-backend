package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname
 * @Description TODO
 * @Date 2022-12-15 10:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class LayoutRefModuleDTO implements Serializable {


    /**
     * 模块或者指标id
     *
     * @author yl
     * @date 2022-12-15 10:49
     * @param null
     * @return
     */
    @NotBlank(message = "模块或者指标id 不能为空")
    private String id;


    /**
     * 模块名称
     */
    private String name;

    /**
     * 前端组件名
     */
    private String viewCode;

    /**
     * 唯一code
     */
    private String code;

    /**
     * 缩略图地址
     */
    private String imageUrl;


}
