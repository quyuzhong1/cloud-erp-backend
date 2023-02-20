package com.erp.common.business.dto.base;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * @Classname PagingDTO
 * @Description TODO
 * @Date 2022-07-12 11:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PagingDTO<T> extends PermissionsDTO {

    @NotNull(message = "当前页码 不能为空")
    private Integer currPage = 1;

    //每页数量
    private Integer pageSize = 10;

    //查询参数
    @NotNull(message = "参数不能为空")
    @Valid
    private T params;

    //排序字符
    private String orderBy;

    /**
     * 排序割断，如：updateTime-DESC
     */
    private static final String SORT_SPLIT = "-";



}
