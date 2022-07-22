package com.cloud.erp.common.common.dto;

import com.cloud.erp.common.utils.FileUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 获取排序的字段
     *
     * @return
     */
    @JsonIgnore
    public String getOrderByColumn() {
        if (StringUtils.isNotBlank(orderBy) && orderBy.indexOf(SORT_SPLIT) != -1) {
            String[] arr = orderBy.split(SORT_SPLIT);
            //驼峰转换下划线
            return FileUtil.humpToUnderLine(arr[0]);
        }
        return "id";
    }

    /**
     * 获取正序或降序
     *
     * @return
     */
    @JsonIgnore
    public boolean getIsAsc() {
        return StringUtils.isNotBlank(orderBy) && orderBy.toLowerCase().indexOf("asc") != -1;
    }


}
