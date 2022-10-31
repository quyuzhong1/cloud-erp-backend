package com.erp.common.modules.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname FindUserDTO
 * @Description TODO
 * @Date 2022-10-08 14:57
 * @Created by yl
 */

@Data
@NoArgsConstructor
public class FindUserDTO  implements Serializable {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 是否是本人 1 是 0 不是
     */
    private Integer isMyState;

}
