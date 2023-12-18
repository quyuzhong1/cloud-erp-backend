package com.erp.oms.aliexpress.handler;


import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.PlatformAliExpressListingDTO;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.request.ProductRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.AliExpressProduct;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.service.AliExpressProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname AliExpressListingHandler
 * @Description TODO
 * @Date 2023-11-29 19:26
 * @Created by yl
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.ALI_EXPRESS)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class AliExpressListingHandler extends AbstractProductHandler<PlatformAliExpressListingDTO, PlatformProductDTO> {

    @Resource
    private AliExpressProductService aliExpressProductService;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Override
    public List<PlatformAliExpressListingDTO> download(JobTaskDTO data) {
        String apiName = AliexpressConstants.LIST_PRODUCT;
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
            log.error("[速卖通商品下载]  获取 token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }
        // 上次执行时间
        LocalDateTime lastTime = data.getLastTime();
        // 下次执行时间
        LocalDateTime nextTime = data.getNextTime();
        String formatStr = DateUtil.fmt;
        ProductRequest productRequest = ProductRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                startTime(LocalDateUtil.formatTime(lastTime, formatStr)).
                endTime(LocalDateUtil.formatTime(nextTime, formatStr)).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(apiName).
                currentPage(1).
                token(shopInfoDTO.getToken()).build();
        List<AliExpressProduct> productList = new ArrayList<>(20);
        try {
            aliExpressProductService.listProduct(productRequest, productList);
        } catch (Exception e) {
            log.error("获取速卖通商品数据异常:{}", e.getMessage());
        }
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }

        // 返回下载源数据
        return productList.stream()
                .map(e -> new PlatformAliExpressListingDTO(e, data))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformProductDTO> convert(List<PlatformAliExpressListingDTO> sourceDataList) {
        List<PlatformProductDTO> resultList = new ArrayList<>(sourceDataList.size());
        for(PlatformAliExpressListingDTO item:sourceDataList){
            resultList.addAll(PlatformAliExpressListingDTO.convertDTO(item));
        }
        return resultList;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}
