package com.erp.server.oms.listing;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.*;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.rest.ShopifyRestClient;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.*;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *Listing单元测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerOmsListingApplicationTests {

    @Resource
    private ListingInfoService listingInfoService;
    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private ShopInfoService shopInfoService;


    @Test
    public void fixMapping() {
        List<ListingInfoEntity> list = listingInfoService.lambdaQuery()
                .ne(ListingInfoEntity::getPlatform, "")
                .list();
        Map<String, SkuMappingEntity> entityMap = skuMappingService.lambdaQuery()
                .ne(SkuMappingEntity::getDictPlatform, "")
                .list()
                .stream()
                .collect(Collectors.toMap(SkuMappingEntity::getListingId, Function.identity()));

        for (ListingInfoEntity entity : list) {

            SkuMappingEntity skuMappingEntity = entityMap.get(entity.getId());
            if (null == skuMappingEntity){
                ShopInfoEntity one = shopInfoService.lambdaQuery()
                        .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                        .eq(ShopInfoEntity::getDictPlatform, entity.getPlatform())
                        .orderByDesc(ShopInfoEntity::getCreateTime)
                        .last("LIMIT 1")
                        .one();
                // 添加到映射
                SkuMappingEntity newEntity = new SkuMappingEntity(entity,one.getId());
                if (!skuMappingService.save(newEntity)) {
                    throw new ServiceException("【listing修复】SkuMapping保存失败");
                }
            }
        }

    }


    @Test
    public void skuList(){
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setSkuNoList(Collections.singletonList("2775"));
        List<ListingInfoWithSkuMappingDTO> skuMappingList = skuMappingService.findListDto(paramDTO);
        System.out.println("结果");
        System.out.println(JSONUtil.toJsonStr(skuMappingList));
    }


}
