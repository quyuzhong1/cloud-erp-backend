package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 专题详情
 *
 * @Classname
 * @Description TODO
 * @Date 2022-12-13 17:11
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SubjectLayoutDetailsDTO implements Serializable {

    /**
     * id
     */
    private String id;


    /**
     * 专题名
     */
    private String name;


    private List<LayoutDetailsDTO> layoutDetailsList;

}
