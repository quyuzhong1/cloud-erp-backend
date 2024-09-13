package com.common.business.dto.base;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.utils.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * @Classname PagingDTO

 * @Date 2022-07-12 11:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PagingDTO<T> extends PermissionsDTO {

    @NotNull(message = "当前页码 不能为空")
    private Integer currPage = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 每页最后的id
     */
    private String lastId;

    /**
     * 查询参数
     */
    @NotNull(message = "参数不能为空")
    @Valid
    private T params;

    /**
     * 排序字符
     */
    private String orderBy;

    /**
     * 排序割断，如：updateTime-DESC
     */
    private static final String SORT_SPLIT = "-";



    public Integer getPage() {
        if (StringUtils.isNotBlank(lastId)) {
            return 1;
        }
        return currPage;
    }

    public Page<T> page() {
        return new Page<>(getPage(), getPageSize());
    }
}
