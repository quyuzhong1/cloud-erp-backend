package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname BasicCategoryTreeDTO
 * @Description TODO
 * @Date 2023-03-02 15:06
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BasicCategoryTreeDTO  implements Serializable {

    /**
     * 分类表id
     */
    private String id;


    /**
     * 名字
     */
    private String name;


    /**
     * 父级id
     */
    private String pid;



    /**
     * 路径
     */
    private String path;

}
