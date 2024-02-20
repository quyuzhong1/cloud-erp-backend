package com.erp.model.bi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName DateDimensionVO
 * @description: TODO
 * @date 2024年02月18日
 * @version: 1.0
 */
@Data
public class DateDimensionVO implements Serializable {
    /**
     * 日期类型
     */
    private String dateType;

    private String dateName;
}
