package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname SalesGroupVO
 * @Description TODO
 * @Date 2022-12-26 10:06
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesGroupVO implements Serializable {


    private String name;

    private List<SalesGroupBaseVO> list;
}
