package com.sdk.oms.tiktok.dto;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.DataBean;
import jodd.util.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
public class TikTokListingDTO extends CleanBaseDTO {
    private DataBean dataBean;

    private String shopId;

    /**
     * 初始化
     */
    public TikTokListingDTO(DataBean dataBean, JobTaskDTO dto) {
        this.dataBean = dataBean;
        this.setIsClean(0);
        this.shopId = dto.getShopId();
        super.setPlatform(PlatformDictEnum.TIK_TOK.getCode());
        this.setUniqueId(dataBean.getSkus().get(0).getFid());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformProductDTO convertDTO(TikTokListingDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformProductDTO initPlatformProductDTO(TikTokListingDTO dto) {

        DataBean dataBean = dto.getDataBean();
        PlatformProductDTO resultDto = new PlatformProductDTO();

        //平台产品id
        resultDto.setPlatformProductNo(dataBean.getFid());
        //平台sku
        resultDto.setPlatformSkuNo(dataBean.getSkus().get(0).getSellerSku());
        if (ObjectUtil.isNotEmpty(dataBean.getSkus().get(0).getSalesAttributes())) {
            //产品规格
            resultDto.setProductSpec(dataBean.getSkus().get(0).getSalesAttributes().get(0).getName() + ":" + dataBean.getSkus().get(0).getSalesAttributes().get(0).getValueName());
        }
        // 平台sku名称
        resultDto.setPlatformSkuName(dataBean.getTitle());
        // 类型 platform 平台  warehouse 仓库
        resultDto.setPlatformType("platform");

        // 平台产品名称
        resultDto.setPlatformProductName(dataBean.getTitle());
        //图片
        if (CollectionUtil.isNotEmpty(dataBean.getMainImages().get(0).getThumbUrls())) {
            resultDto.setProductImageUrl(dataBean.getMainImages().get(0).getThumbUrls().get(0));
        } else {
            resultDto.setProductImageUrl(dataBean.getMainImages().get(0).getUrls().get(0));
        }

        resultDto.setShopId(dto.getShopId());
        resultDto.setPlatformUpdateTime(LocalDateTime.now());

        //包装信息
        if (ObjectUtil.isNotEmpty(dataBean.getPackageDimensions())) {
            String productPacking = "";
            if (StringUtil.isNotBlank(dataBean.getPackageDimensions().getLength())
                    && StringUtil.isNotBlank(dataBean.getPackageDimensions().getUnit())) {
                productPacking = productPacking + dataBean.getPackageDimensions().getLength() + dataBean.getPackageDimensions().getUnit();
            }
            if (StringUtil.isNotBlank(dataBean.getPackageDimensions().getWidth())
                    && StringUtil.isNotBlank(dataBean.getPackageDimensions().getUnit())) {
                productPacking = productPacking + dataBean.getPackageDimensions().getWidth() + dataBean.getPackageDimensions().getUnit();
            }
            if (StringUtil.isNotBlank(dataBean.getPackageDimensions().getHeight())
                    && StringUtil.isNotBlank(dataBean.getPackageDimensions().getUnit())) {
                productPacking = productPacking + dataBean.getPackageDimensions().getHeight() + dataBean.getPackageDimensions().getUnit();
            }
            resultDto.setProductPacking(productPacking);
        }

        // 平台类型
        resultDto.setPlatform(dto.getPlatform());
        // 唯一ID
        resultDto.setUniqueId(dto.getUniqueId());
        // 同步任务ID
        resultDto.setDmpSyncTaskId(dto.getDmpSyncTaskId());
        return resultDto;
    }
}
