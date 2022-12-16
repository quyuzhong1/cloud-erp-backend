package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname AdvanceSearchDTO
 * @Description TODO
 * @Date 2022-12-16 9:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AdvanceSearchDTO  implements Serializable {


    /**
     * 搜索类型
     */
    private String searchType;


    /**
     * 搜索内容
     */
    private String searchContent;
}
