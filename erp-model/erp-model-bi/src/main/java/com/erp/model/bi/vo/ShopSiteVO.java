package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 店铺分类
 * @Classname
 * @Description TODO
 * @Date 2022-12-28 17:49
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ShopSiteVO implements Serializable {


    /**
     * 分类名
     */
    private String site;

    private List<String> shopNo;
}
