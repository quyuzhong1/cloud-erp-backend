package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname UserInfoDTO
 * @Description TODO
 * @Date 2022-10-31 14:46
 * @Created by yl
 */
@Data
public class UserInfoDTO  implements Serializable {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;
}
