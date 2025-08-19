package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname DictBasicDTO

 * @Date 2023-03-16 16:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictBasicDTO implements Serializable {


    /**
     * 表id
     */
    private String id;

    /**
     * value 使用值
     */
    private String value;

    /**
     * type 分组
     */
    private String type;

    /**
     * 名称
     */
    private String typeName;

    /**
     * 名称
     */
    private String name;
    /**
     * 备注
     */
    private String remark;

    /**
     * 子级，级联的时候使用
     */
    private List<DictBasicDTO> childList;
}
