package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname
 * @Description TODO
 * @Date 2022-12-26 15:53
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuCategoryVO implements Serializable {

    /**
     * 分类名
     */
    private String name;

    private List<String> skuList;


}
