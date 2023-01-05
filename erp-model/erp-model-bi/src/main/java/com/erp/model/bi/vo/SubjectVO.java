package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SubjectVO
 * @Description TODO
 * @Date 2023-01-05 18:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SubjectVO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * name
     */
    private String name;

    /**
     * 分类id
     */
    private String categoryId;


    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 分享是否可见 true可见
     */
    private boolean shareVisible = false;

    /**
     *我自己创建 可见
     */
    private boolean myCreateVisible = false;
}
