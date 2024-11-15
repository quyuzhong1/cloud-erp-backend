package com.erp.tms.batong.model.label.base;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname BaseData

 * @Date 2024-01-15 12:07
 * @Created by yl
 */
@Data
public class BaseData  implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 代码
     */
    @Alias("code")
    private String code;

    /**
     * 中文名称
     */
    @Alias("cnname")
    private String cnName;


    /**
     * 英文名称
     */
    @Alias("enname")
    private String enName;

    /**
     * 备注
     */
    @Alias("note")
    private String note;
}
