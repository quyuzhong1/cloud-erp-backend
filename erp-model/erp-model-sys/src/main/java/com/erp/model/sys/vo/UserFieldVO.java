package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname UserFieldVO
 * @Description TODO
 * @Date 2023-02-09 10:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UserFieldVO implements Serializable {


    /**
     * 反选字段
     */
    private List<String> invertList;

    /**
     * 选中的字段
     */
    private List<String> checkList;

    private String moduleCode;

    private String moduleName;
}
