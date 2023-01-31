package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/14 10:34
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserSuperiorDTO implements Serializable {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 上级类型
     */
    private String superiorType;
}
