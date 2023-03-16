package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 供应商阶段分页 入参
 * @author Lambda
 * @Classname SupplierPagingParamDTO
 * @Description TODO
 * @Date 2023-03-15 18:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPhasePagingParamDTO extends SortDTO {


    @StateEnumValue(strValues = {"all","waitAudit"}, message = "搜索类型有误")
    private String searchType;

    /**
     * 供应商名
     */
    private String name;


    /**
     * 分类id集合
     */
    private List<String> categoryIdList;


    /**
     * 阶段列表
     */
    private List<String> phaseList;



    /**
     * 联系人名
     */
    private String contactPerson;


    /**
     * 联系人
     */
    private String contactPhone;
}
