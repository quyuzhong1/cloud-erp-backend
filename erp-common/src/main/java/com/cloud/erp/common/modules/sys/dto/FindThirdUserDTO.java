package com.cloud.erp.common.modules.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname FindThirdUserInfo
 * @Description TODO
 * @Date 2022-07-26 10:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindThirdUserDTO implements Serializable {

    private String code;

    private String thirdType;
}
