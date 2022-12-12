package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname LayoutDTO
 * @Description TODO
 * @Date 2022-12-09 16:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class LayoutDTO  implements Serializable {


    /**
     * 高度
     */
    private Integer height=0;

    /**
     * 块的编号
     */
    private String blockNo;


    /**
     * 模块id集合
     */
    private List<String>  moduleIdList;
}
