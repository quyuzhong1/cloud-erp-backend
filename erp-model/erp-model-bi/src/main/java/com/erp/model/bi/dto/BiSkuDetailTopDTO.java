package com.erp.model.bi.dto;

import com.erp.model.bi.vo.LabelVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 平台SKU详情
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class BiSkuDetailTopDTO {

    /**
     * 新品标识 ture 为新品 false 为非新品
     */
    private boolean hasNewSign = true;

    /**
     * SKU NO
     */
    private String skuNo;

    /**
     * SKU中文名
     */
    private String nameCn;

    /**
     * 英文名
     */
    private String nameEn;

    /**
     * 商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
     */
    private Integer status;

    /**
     * 商品目录(一级)
     */
    private String parentCategoryName;

    /**
     * 商品目录(二级)
     */
    private String categoryName;

    /**
     * 开发员id
     */
    private String developerId;

    /**
     * 开发员名称
     */
    private String developerName;

    /**
     * 产品负责人/产品经理id
     */
    private String chargeId;

    /**
     * 产品负责人/产品经理名称
     */
    private String chargeName;

    /**
     * 示意图url
     */
    private String imageUrl;

    /**
     * 公司首单日期
     */
    private String firstOrderDate = "";

    /**
     * 各平台首单日期
     */
    private List<String> platformFirstOrderDate = new ArrayList<>();
    /**
     * 标签列表
     */
    private List<LabelVO> labels;
}