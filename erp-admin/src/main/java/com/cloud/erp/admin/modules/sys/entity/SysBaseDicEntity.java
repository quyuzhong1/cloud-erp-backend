package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Classname SysBaseDicEntity
 * @Description TODO
 * @Date 2022-07-20 16:50
 * @Created by yl
 */
@Data
@TableName("sys_base_dic")
public class SysBaseDicEntity {


    private Long id;

    private String dicValue;

    private String dicType;

    private String dicTitle;

}
