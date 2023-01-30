package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;

/**  变更详情
 * @Classname
 * @Description TODO
 * @Date 2023-01-28 16:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductChangeDTO implements Serializable {


    /**
     *
     */
    private String id;

    /**
     * 类型
     */
    private String type;


    /**
     * 来源的信息
     */
    private String sourceId;

    private T  info;
}
