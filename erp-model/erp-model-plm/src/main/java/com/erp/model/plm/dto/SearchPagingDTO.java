package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname BomSearchPagingDTO
 * @Description TODO
 * @Date 2023-01-10 17:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SearchPagingDTO implements Serializable {

    @StateEnumValue(strValues = {"all","waitAudit"}, message = "搜索类型有误")
    private String searchType;


    /**
     * 搜索关键字
     */
    private String searchKeyword;

}
