package com.erp.server.oms.listing;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Resource
    private SoB2cService soB2cService;


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
        paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(Collections.singletonList("EU11-B012GBB1"));
        paramDTO.setShopIdList(Collections.singletonList("1734476072977698818"));
//        paramDTO.setIsExpire(false);
        paramDTO.setLastExpireDate(LocalDateTime.parse("2024-03-01 17:30:16", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        paramDTO.setIsExpire(false);
        List<ListingInfoWithSkuMappingDTO> skuMappingList = skuMappingService.findListDto(paramDTO);
        System.out.println("结果");
        System.out.println(JSONUtil.toJsonStr(skuMappingList));
    }

    @Test
    public void test(){
        SoOutstockDTO.GenerateB2cDTO soOutstockInfoById = soB2cService.getSoOutstockInfoById("1773704392155860993");
        System.out.println(soOutstockInfoById);
    }


}
