package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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
     * @author yl
     * @date 2022-12-15 10:49
     * @param null
     * @return 
     */
    private String id;

    /**
     * 类型
     * module 模块  target 指标
     */
    private String type;
}
