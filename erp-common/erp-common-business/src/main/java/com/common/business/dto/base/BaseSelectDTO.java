package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @CreateTime: 2023-06-12  16:01
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseSelectDTO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

}