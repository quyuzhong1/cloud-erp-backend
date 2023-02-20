package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname BomSearchPagingDTO
 * @Description TODO
 * @Date 2023-01-10 17:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SearchPagingDTO extends SortDTO {

    @StateEnumValue(strValues = {"all","waitAudit"}, message = "搜索类型有误")
    private String searchType;


    /**
     * 搜索关键字
     */
    private String searchKeyword;

}
