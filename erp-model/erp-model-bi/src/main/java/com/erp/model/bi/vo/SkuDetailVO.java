package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname
 * @Date 2022-12-26 15:53
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuDetailVO implements Serializable {

    /**
     * skuNo
     */
    private String skuNo;
    /**
     * skuId
     */
    private String skuId;
    /**
     * 标签列表
     */
    private List<LabelVO> labelVOS;


}
