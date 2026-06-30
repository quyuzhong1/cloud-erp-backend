package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Will
 * @version 1.0

 * @date 2022/11/21 11:54
 */
@Data
@NoArgsConstructor
public class SysCodeDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 类目(编号前缀)
     */
    private String category;

    /**
     * 顺序码
     */
    private Integer num;

    /**
     * 编码类型 (枚举BusinessNoTypeEnum)
     */
    private String type;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    public SysCodeDTO (String category,String type) {
        this.category = category;
        this.type = type;
    }

}
