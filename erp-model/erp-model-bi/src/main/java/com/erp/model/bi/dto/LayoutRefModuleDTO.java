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
     * 系统模块id
     */
    private String sysModuleId;

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


    /**
     * 是否可见  true 可见
     * false 不可见
     */
    private Boolean visible;



    @Data
    @NoArgsConstructor
    public static class LayoutRefModuleInfoDTO{
        /**
         * 布局id
         */
        private String layoutId;

        /**
         * 模块id
         */
        private String moduleId;


    }

}
