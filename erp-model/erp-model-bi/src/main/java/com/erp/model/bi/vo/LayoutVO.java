package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Classname LayoutVO
 * @Description TODO
 * @Date 2023-01-07 10:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class LayoutVO implements Serializable {

    /**
     * 布局id
     */
    private String layoutId;

    /**
     * 创建时间
     */
    private LocalDateTime subjectCreateTime;

}
