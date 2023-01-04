package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Classname XyAxesResultVO
 * @Description TODO
 * @Date 2023-01-03 17:32
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class XyAxesResultVO implements Serializable {


    private List<XAxesVO> columnList;

    private List<Map<String,Object>> rowList;
}
