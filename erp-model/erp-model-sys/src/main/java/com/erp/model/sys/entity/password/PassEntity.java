package com.erp.model.sys.entity.password;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname PassEntity
 * @Description TODO
 * @Date 2022-07-06 11:47
 * @Created by yl
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassEntity {

    /**
     * 密码随机串码
     */
    private String salt;


    /**
     * MD5后的密码
     */
    private String password;
}
