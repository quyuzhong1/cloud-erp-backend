package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname UserFieldVO
 * @Description TODO
 * @Date 2023-02-09 10:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UserFieldVO implements Serializable {


    private String layoutJson;

    private String moduleCode;

    private String moduleName;
}
