package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname CategorySubjectDTO
 * @Description TODO
 * @Date 2022-12-14 16:06
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CategorySubjectDTO implements Serializable {

    /**
     * 分类id
     */
    private String categoryId="";


    /**
     * 分类名
     */
    private String categoryName;

    /**
     * 分类的专题
     */

    private List<SubjectDTO> subjectList;


}
