package com.erp.common.dto.base;


import io.swagger.annotations.ApiModelProperty;
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
public class PagingDTO<T> {

    @NotNull(message = "当前页码 不能为空")
    @ApiModelProperty(value = "页码")
    private Integer currPage = 1;

    //每页数量
    @ApiModelProperty(value = "页数")
    private Integer pageSize = 10;

    //查询参数
    @NotNull(message = "参数不能为空")
    @Valid
    @ApiModelProperty(value = "查询参数")
    private T params;

    //排序字符
    private String orderBy;

    /**
     * 排序割断，如：updateTime-DESC
     */
    private static final String SORT_SPLIT = "-";



}
