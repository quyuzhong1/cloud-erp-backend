package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 16:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseDropDownDTO implements Serializable {

    /**
     * 编码
     */
    private String code;
    /**
     * 值
     */
    private String value;
    /**
     * 描述
     */
    private String desc;
}
