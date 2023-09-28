package com.erp.model.bi.dto;

import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.entity.BiProductInfoEntity;
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
    private boolean hasNewSign;

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
     * 销售状态: 0=无, 1.未销售 2.销售中 3.清仓中 4.已下架
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
    private String firstOrderDate;

    /**
     * 各平台首单日期
     */
    private List<String> platformFirstOrderDate;
    /**
     * 标签列表
     */
    private List<LabelVO> labels;

    public BiSkuDetailTopDTO(BiProductDetailEntity detailEntity,
                             BiProductInfoEntity productInfo,
                             Integer scaleStatus
    ) {
        this.hasNewSign = true;
        this.skuNo = detailEntity.getSkuNo();
        this.nameCn = detailEntity.getName();
        this.nameEn = "";
        this.status = scaleStatus;
        this.parentCategoryName = "";
        this.categoryName =productInfo.getCategory();
        this.chargeId = detailEntity.getChargeId();
        this.chargeName = detailEntity.getChargeName();
        this.imageUrl = detailEntity.getImagesUrl();
        this.firstOrderDate = "";
        this.platformFirstOrderDate = new ArrayList<>();
        this.labels = new ArrayList<>();
    }
}