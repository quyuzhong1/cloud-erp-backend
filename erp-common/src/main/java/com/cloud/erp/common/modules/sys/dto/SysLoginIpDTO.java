package com.cloud.erp.common.modules.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname SysLoginIpDTO
 * @Description TODO
 * @Date 2022-07-15 12:23
 * @Created by yl
 */
@NoArgsConstructor
@Data
public class SysLoginIpDTO implements Serializable {


    private String ip;

    private Date date;

    private String uid;
}
