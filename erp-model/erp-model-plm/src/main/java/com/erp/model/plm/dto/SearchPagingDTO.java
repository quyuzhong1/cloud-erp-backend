package com.erp.model.plm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.core.anno.StateEnumValue;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Classname BomSearchPagingDTO

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

    /**
     * 页面高级查询
     */
    private List<AdvanceQueryDTO> advanceQueryDTOList;

    /**
     * sqlMap 默认key default
     */
    private Map<String, String> sqlMap;

}
