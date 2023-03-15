package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 供应商分页 入参
 * @author Lambda
 * @Classname SupplierPagingParamDTO
 * @Description TODO
 * @Date 2023-03-15 18:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPagingParamDTO  extends SortDTO {


    /**
     * 供应商名
     */
    private String name;


    /**
     * 分类id集合
     */
    private List<String> categoryIdList;


    /**
     * 生命周期集合
     */
    private List<String> lifeCycleList;



    /**
     * 联系人名
     */
    private String contactPerson;


    /**
     * 联系人
     */
    private String contactPhone;
}
