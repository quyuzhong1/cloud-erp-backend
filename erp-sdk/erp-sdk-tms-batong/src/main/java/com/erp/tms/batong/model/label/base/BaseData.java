package com.erp.tms.batong.model.label.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname BaseData
 * @Description TODO
 * @Date 2024-01-15 12:07
 * @Created by yl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseData  implements Serializable {


    /**
     * 代码
     */
    @JSONField(name = "code")
    private String code;

    /**
     * 中文名称
     */
    @JSONField(name = "cnname")
    private String cnName;


    /**
     * 英文名称
     */
    @JSONField(name = "enname")
    private String enName;

    /**
     * 备注
     */
    @JSONField(name = "note")
    private String note;
}
