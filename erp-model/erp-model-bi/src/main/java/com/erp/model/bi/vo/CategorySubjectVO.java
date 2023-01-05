package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname CategorySubjectVO
 * @Description TODO
 * @Date 2023-01-05 18:39
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CategorySubjectVO implements Serializable {

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

    private List<SubjectVO> subjectList;
}
