package com.sdk.oms.mercado.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.google.common.collect.Lists;
import com.sdk.oms.mercado.dto.MercadoListingDTO;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.ListingDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ResultsBean;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 沃尔玛商品信息
 * @Author Luo_WG
 * @Date 2023/10/18 11:24
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.MERCADO)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class MercadoListingHandler extends AbstractProductHandler<MercadoListingDTO, PlatformProductDTO> {

    public static void main(String[] args) {
        //TG-65ddb89790e8fe00014506db-1509269799
        MercadoSdkClientService mercadoSdkClientService = new MercadoSdkClientService();
        String accessToken = "APP_USR-3457166802805723-030103-014e885bfebbe93566572374790f6cac-1509269799";
        List<ResultsBean> resultsBeanList = new ArrayList<>();
        //请求参数
        HashMap<String, Object> paramMap = new HashMap<>();
        //每次最多获取200条
        Integer pageSize = 200;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        while(pageNo < pageCount) {
    //https://api.mercadolibre.com/users/$USER_ID/items/search?search_type=scan&scroll_id=eyJpZCI6IkNCVDE4ODcxMDcxODUiLCJudW1lcmljX2lkIjoxODg3MTA3MTg1LCJzdG9wX3RpbWUiOiIyMDQ0LTAxLTA2VDA0OjAwOjAwLjAwMFoifQ==
            //https://api.mercadolibre.com/marketplace/products/search?status=active&product_identifier=%s
//            String baseUrl = "https://api.mercadolibre.com/marketplace/products/search?q=vacuum%20wireless&limit=100&status=inactive&offset=10'";

            String baseUrl = "https://api.mercadolibre.com/users/1509269799/items/search";
            StringBuffer sb = new StringBuffer();
            sb.append(baseUrl);
            sb.append("?limit="+pageSize+"");
            sb.append("&offset="+pageNo+"");

            //拉取数据
            String date = mercadoSdkClientService.sendMercadoGet(sb.toString(), accessToken, paramMap);
            System.out.println(date);
            return;
           /* PlatformMercadoListingDTO mercadoListingDTO = JSONUtil.toBean(date, PlatformMercadoListingDTO.class);
            if (CollectionUtils.isEmpty(mercadoListingDTO.getResults())) {
                throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), date);
            }
            pageCount = (mercadoListingDTO.getPaging().getTotal() + pageSize - 1) / pageSize;

            pageNo++;

            resultsBeanList.addAll(mercadoListingDTO.getResults());*/
        }
            System.out.println(JSONUtil.toJsonStr(resultsBeanList));
    }


    @Resource
    private MercadoSdkClientService mercadoSdkClientService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    public List<MercadoListingDTO> download(JobTaskDTO data) {
        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoDTO = this.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
            log.error("[美客多商品下载]  获取 token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }

        List<ListingDTO> resultsBeanList = new ArrayList<>();
        //请求参数
        HashMap<String, Object> paramMap = new HashMap<>();
        //每次最多获取200条
        Integer pageSize = 200;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        while(pageNo < pageCount) {

            //https://api.mercadolibre.com/marketplace/products/search?status=active&product_identifier=%s
            String baseUrl = "https://api.mercadolibre.com/users/"+shopInfoDTO.getUserId()+"/items/search";
            StringBuffer sb = new StringBuffer();
            sb.append(baseUrl);
            sb.append("?limit="+ pageSize +"");
            sb.append("&offset="+ pageNo +"");

            //拉取数据
            String date = mercadoSdkClientService.sendMercadoGet(baseUrl, shopInfoDTO.getAccessToken(), paramMap);

            ListingDTO platformMercadoListingDTO = JSONUtil.toBean(date, ListingDTO.class);
            if (CollectionUtils.isEmpty(platformMercadoListingDTO.getResults())) {
                break;
            }
            pageCount = (platformMercadoListingDTO.getPaging().getTotal() + pageSize - 1) / pageSize;

            pageNo++;

            List<String> results = platformMercadoListingDTO.getResults();
            List<List<String>> partition = Lists.partition(results, 20);

        }

        if (CollectionUtils.isEmpty(resultsBeanList)) {
            return Collections.emptyList();
        }
/*
        // 返回下载源数据
        return resultsBeanList.stream()
                .map(e -> new MercadoListingDTO(e, data))
                .collect(Collectors.toList());*/
        return null;
    }

    @Override
    public List<PlatformProductDTO> convert(List<MercadoListingDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(MercadoListingDTO::convertDTO).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }


    //http://api.mercadolibre.com/items?ids=CBT910553725,CBT910547444


    public MercadoShopInfoDTO listItemView() {
        /*String baseUrl = "http://api.mercadolibre.com/items?ids=";
        StringBuffer sb = new StringBuffer();
        sb.append(baseUrl);

        //拉取数据
        String date = mercadoSdkClientService.sendMercadoGet(baseUrl, shopInfoDTO.getAccessToken(), paramMap);*/
        return null;
    }

    /**
     * 查询店铺信息
     * @param shopId
     * @return
     */
    public MercadoShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADO.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof MercadoShopInfoDTO) {
                return (MercadoShopInfoDTO) tokenObj;
            }
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.MERCADO_ACCESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }
            MercadoShopInfoDTO result = new MercadoShopInfoDTO();
            result.setBaseUrl(cfgAppClient.getUrl());
            result.setClientId(cfgAppClient.getClientId());
            result.setClientSecret(cfgAppClient.getClientSecret());
            result.setId(shopId);
            if (Objects.nonNull(shopAuthEntity)) {
                result.setAccessToken(shopAuthEntity.getAccessToken());
                redisUtil.set(tokenKey, result, shopAuthEntity.getExpiresIn());
            }

            return result;
        }
        return null;

    }

}
