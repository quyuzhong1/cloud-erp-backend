package com.erp.oms.aliexpress.dto;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.oms.aliexpress.dto.response.AliExpressProduct;
import com.erp.oms.aliexpress.dto.response.AliExpressProductDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname PlatformAliExpressListingDTO
 * @Description
 * @Date 2023-11-30 11:24
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAliExpressListingDTO extends CleanBaseDTO {

    private AliExpressProduct aliExpressProduct;

    /**
     * 初始化
     */
    public PlatformAliExpressListingDTO(AliExpressProduct aliExpressProduct, JobTaskDTO dto) {
        this.aliExpressProduct = aliExpressProduct;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        this.setUniqueId(aliExpressProduct.getProductId().toString());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }


    /**
     * 将下载的产品数据转化成想要的数据
     *
     * @param dto
     * @return
     * @author yl
     * @date 2023-11-30 14:50
     */
    public static List<PlatformProductDTO> convertDTO(PlatformAliExpressListingDTO dto) {
        if (Objects.isNull(dto)) {
            return Collections.emptyList();
        }
        AliExpressProduct sourceProduct = dto.getAliExpressProduct();
        List<AliExpressProductDetail> detailList = sourceProduct.getProductDetailList();
        List<PlatformProductDTO> resultList = new ArrayList<>(detailList.size());
        for (AliExpressProductDetail item : detailList) {
            PlatformProductDTO product = new PlatformProductDTO();
            product.setPlatform(dto.getPlatform());
            // 平台sku no
            product.setPlatformProductNo(sourceProduct.getProductId().toString());
            // 平台sku 名
            product.setPlatformProductName(sourceProduct.getSubject());
            product.setPlatformSkuNo(item.getSkuCode());
            // 类型 platform 平台  warehouse 仓库
            product.setPlatformType("platform");
            product.setProductImageUrl(sourceProduct.getImageUrls());
            // 包装信息
            String packing = StrUtil.format("长度:{}cm;宽度:{}cm;高度:{}cm;重量:{}kg;", sourceProduct.getPackageLength(), sourceProduct.getPackageWidth(), sourceProduct.getPackageHeight(), sourceProduct.getGrossWeight());

            product.setProductPacking(packing);
            String gmtModified = sourceProduct.getGmtModified();
            product.setPlatformUpdateTime(LocalDateUtil.parseStrToLocalTime(gmtModified));
            resultList.add(product);
        }

        return resultList;
    }
}
